package no.nav.saf.tjeneste.dokumentoversiktbruker;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import no.nav.saf.domain.TilgangsmodellRepository;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.Pep;
import no.nav.saf.tjeneste.visningsmodell.DokumentInfo;
import no.nav.saf.tjeneste.visningsmodell.Dokumentoversikt;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class DokumentoversiktBrukerCoordinatorImpl implements DokumentoversiktBrukerCoordinator {

	private final SideInfoMapper sideInfoMapper = new SideInfoMapper();
	private final TilgangsmodellRepository tilgangsmodellRepository;
	private final DokumentoversiktBrukerVisningsmodellRepository visningsmodellRepository;
	private final Pep<TilgangBruker> pep1;
	private final Pep<TilgangSak> pep2;
	private final Pep<TilgangSak> pep3;
	private final Pep<TilgangJournalpost> pep4;

	@Inject
	public DokumentoversiktBrukerCoordinatorImpl(TilgangsmodellRepository tilgangsmodellRepository,
												 DokumentoversiktBrukerVisningsmodellRepository visningsmodellRepository,
												 @Named("pep1") Pep<TilgangBruker> pep1,
												 @Named("pep2") Pep<TilgangSak> pep2,
												 @Named("pep3") Pep<TilgangSak> pep3,
												 @Named("pep4") Pep<TilgangJournalpost> pep4) {
		this.tilgangsmodellRepository = tilgangsmodellRepository;
		this.visningsmodellRepository = visningsmodellRepository;
		this.pep1 = pep1;
		this.pep2 = pep2;
		this.pep3 = pep3;
		this.pep4 = pep4;
	}

	@Override
	public Dokumentoversikt hentDokumentoversikt(DokumentoversiktBrukerArguments dokumentoversiktBrukerArguments, SafRequestContext safRequestContext) {
		final TilgangBruker tilgangBruker = tilgangsmodellRepository.findTilgangBruker(dokumentoversiktBrukerArguments.getBrukerIdInput());
		safRequestContext.getRequestCache().putObject("tilgangBruker", tilgangBruker);
		boolean pep1Access = this.pep1.hasAccess(tilgangBruker, safRequestContext);

		if (!pep1Access) {
			return Dokumentoversikt.empty();
		}

		final List<TilgangSak> tilgangSakList = tilgangsmodellRepository.findTilgangSaker(tilgangBruker, dokumentoversiktBrukerArguments.getTema(), safRequestContext);
		List<TilgangSak> filteredTilgangSakList = Flowable.fromIterable(tilgangSakList)
				.flatMap(tilgangSak ->
						Flowable.just(tilgangSak)
								.observeOn(Schedulers.io())
								.filter(ts -> pep2.hasAccess(ts, safRequestContext))
								.filter(ts -> pep3.hasAccess(ts, safRequestContext))
				).toList()
				.blockingGet();

		final List<TilgangJournalpost> tilgangJournalpostList = tilgangsmodellRepository.findTilgangJournalposter(
				tilgangBruker,
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
				.flatMap(tilgangJournalpost ->
						Flowable.just(tilgangJournalpost)
								.observeOn(Schedulers.io())
								.filter(tj -> pep4.hasAccess(tj, safRequestContext))
				).toList()
				.blockingGet();

		List<Journalpost> visningJournalposter = visningsmodellRepository.findJournalposter(filteredTilgangJournalpostList.stream()
				.map(TilgangJournalpost::getJournalpostId)
				.sorted(Comparator.reverseOrder())
				.collect(Collectors.toList()), safRequestContext);

		return Dokumentoversikt.builder()
				.journalposter(visningJournalposter)
				.sideInfo(sideInfoMapper.mapSideInfo(dokumentoversiktBrukerArguments, visningJournalposter))
				.build();
	}

	@Override
	public List<Journalpost> findJournalposter(final DokumentoversiktBrukerArguments dokumentoversiktBrukerArguments, final SafRequestContext safRequestContext) {
		return new ArrayList<>();
	}

	@Override
	public List<DokumentInfo> findDokumenter(final Journalpost journalpost, final SafRequestContext safRequestContext) {
		// TODO MMA-1092 Pep4 for TilgangDokument her (er dette allerede er avklart i TilgangJournalpost så må context vite om dette)
		return journalpost.getDokumenter();
	}
}
