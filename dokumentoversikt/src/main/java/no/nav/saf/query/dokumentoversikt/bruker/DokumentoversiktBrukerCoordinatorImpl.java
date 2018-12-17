package no.nav.saf.query.dokumentoversikt.bruker;

import static no.nav.saf.domain.DomainConstants.TILGANG_BRUKER;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.pensjonsak.hentBrukerOgEnhetstilgangerForSak.HentBrukerForSakResponseTo;
import no.nav.saf.anticorruptionlayer.pensjonsak.hentBrukerOgEnhetstilgangerForSak.PensjonSakRestConsumer;
import no.nav.saf.domain.TilgangsmodellRepository;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.query.dokumentoversikt.DokumentoversiktVisningsmodellRepository;
import no.nav.saf.query.dokumentoversikt.SideInfoMapper;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.Pep;
import no.nav.saf.tjeneste.visningsmodell.Dokumentoversikt;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
class DokumentoversiktBrukerCoordinatorImpl implements DokumentoversiktBrukerCoordinator {

	private final SideInfoMapper sideInfoMapper = new SideInfoMapper();
	private final TilgangsmodellRepository tilgangsmodellRepository;
	private final DokumentoversiktVisningsmodellRepository visningsmodellRepository;
	private final Pep<TilgangBruker> pep1;
	private final Pep<TilgangSak> pep2;
	private final Pep<TilgangSak> pep2d;
	private final Pep<TilgangSak> pep3;
	private final Pep<TilgangJournalpost> pep4;

	@Inject
	PensjonSakRestConsumer pensjonSakRestConsumer;

	@Inject
	public DokumentoversiktBrukerCoordinatorImpl(TilgangsmodellRepository tilgangsmodellRepository,
												 DokumentoversiktVisningsmodellRepository visningsmodellRepository,
												 @Named("pep1") Pep<TilgangBruker> pep1,
												 @Named("pep2") Pep<TilgangSak> pep2,
												 @Named("pep2d") Pep<TilgangSak> pep2d,
												 @Named("pep3") Pep<TilgangSak> pep3,
												 @Named("pep4") Pep<TilgangJournalpost> pep4) {
		this.tilgangsmodellRepository = tilgangsmodellRepository;
		this.visningsmodellRepository = visningsmodellRepository;
		this.pep1 = pep1;
		this.pep2 = pep2;
		this.pep2d = pep2d;
		this.pep3 = pep3;
		this.pep4 = pep4;
	}

	@Override
	public Dokumentoversikt hentDokumentoversikt(DokumentoversiktBrukerArguments dokumentoversiktBrukerArguments, SafRequestContext safRequestContext) {

		HentBrukerForSakResponseTo hentBrukerForSakResponseTo = pensjonSakRestConsumer.hentBrukerForSak("22410742");
		TilgangBruker tilgangBruker = tilgangsmodellRepository.findTilgangBruker(dokumentoversiktBrukerArguments.getBrukerIdInput());
		safRequestContext.getRequestCache().putObject(TILGANG_BRUKER, tilgangBruker);

		boolean pep1Access = this.pep1.hasAccess(tilgangBruker, safRequestContext);

		if (!pep1Access) {
			return Dokumentoversikt.empty();
		}

		final Flowable<TilgangSak> tilgangSakFlow = tilgangsmodellRepository.findTilgangSaker(tilgangBruker, dokumentoversiktBrukerArguments
				.getTema(), safRequestContext);
		List<TilgangSak> filteredTilgangSakList = tilgangSakFlow
				.onErrorResumeNext(throwable -> {
					return Flowable.empty();
				})
				.parallel(10)
				.runOn(Schedulers.io())
				.filter(ts -> pep2.hasAccess(ts, safRequestContext))
				.filter(ts -> pep2d.hasAccess(ts, safRequestContext))
				.filter(ts -> pep3.hasAccess(ts, safRequestContext))
				.sequential()
				.toList().blockingGet();

		final List<TilgangJournalpost> tilgangJournalpostList = tilgangsmodellRepository.findTilgangJournalposter(
				Collections.singletonList(tilgangBruker),
				filteredTilgangSakList,
				dokumentoversiktBrukerArguments.getFraDato(),
				dokumentoversiktBrukerArguments.getTema(),
				dokumentoversiktBrukerArguments.getJournalposttyper(),
				dokumentoversiktBrukerArguments.getJournalstatuser(),
				dokumentoversiktBrukerArguments.getFoerste(),
				dokumentoversiktBrukerArguments.getEtterPeker(),
				dokumentoversiktBrukerArguments.getSiste(),
				dokumentoversiktBrukerArguments.getFoerPeker(),
				safRequestContext
		);

		final List<TilgangJournalpost> filteredTilgangJournalpostList = Flowable.fromIterable(tilgangJournalpostList)
				.parallel(10)
				.runOn(Schedulers.io())
				.filter(tj -> pep4.hasAccess(tj, safRequestContext))
				.sequential()
				.toList()
				.blockingGet();

		List<Journalpost> visningJournalposter = visningsmodellRepository.findJournalposter(filteredTilgangJournalpostList.stream()
				.map(TilgangJournalpost::getJournalpostId)
				.sorted(Comparator.reverseOrder())
				.collect(Collectors.toList()), safRequestContext);

		return Dokumentoversikt.builder()
				.journalposter(visningJournalposter)
				.sideInfo(sideInfoMapper.mapSideInfo(visningJournalposter, safRequestContext))
				.build();
	}
}
