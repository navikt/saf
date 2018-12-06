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
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class DokumentoversiktFagsakCoordinatorImpl implements DokumentoversiktFagsakCoordinator {

	private final TilgangsmodellRepository tilgangsmodellRepository;
	private final DokumentoversiktBrukerVisningsmodellRepository visningsmodellRepository;
	private final Pep<TilgangBruker> pep1;
	private final Pep<TilgangSak> pep2;
	private final Pep<TilgangSak> pep3;
	private final Pep<TilgangJournalpost> pep4;

	@Inject
	public DokumentoversiktFagsakCoordinatorImpl(TilgangsmodellRepository tilgangsmodellRepository,
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
	public List<Journalpost> findJournalposter(final DokumentoversiktFagsakArguments dokumentoversiktFagsakArguments, final SafRequestContext safRequestContext) {
		final String fagsakId = dokumentoversiktFagsakArguments.getFagsakId();
		final String fagsaksystem = dokumentoversiktFagsakArguments.getFagsaksystem();

		final List<TilgangBruker> tilgangBrukerList = tilgangsmodellRepository.findTilgangBrukerList(fagsakId, fagsaksystem);
		tilgangBrukerList.stream()
				.forEach(tilgangBruker -> safRequestContext.getParameterContext().putParameter("tilgangBruker", tilgangBruker));


		List<TilgangBruker> filteredTilgangBrukerList = Flowable.fromIterable(tilgangBrukerList)
				.flatMap(tilgangBruker ->
						Flowable.just(tilgangBruker)
								.observeOn(Schedulers.io())
								.filter(ts -> pep1.hasAccess(ts, safRequestContext))
				).toList()
				.blockingGet();

		final List<String> filteredAktoerIdListTilgangBruker = filteredTilgangBrukerList.stream()
				.map(tilgangBruker -> tilgangBruker.getAktoerId())
				.collect(Collectors.toList());

		final List<TilgangSak> tilgangSakList = tilgangsmodellRepository.findTilgangSakList(fagsakId, fagsaksystem).stream()
				.filter(tilgangSak -> filteredAktoerIdListTilgangBruker.contains(tilgangSak.getAktoerId()))
				.collect(Collectors.toList());

		List<TilgangSak> filteredTilgangSakList = Flowable.fromIterable(tilgangSakList)
				.flatMap(tilgangSak ->
						Flowable.just(tilgangSak)
								.observeOn(Schedulers.io())
								.filter(ts -> pep2.hasAccess(ts, safRequestContext))
								.filter(ts -> pep3.hasAccess(ts, safRequestContext))
				).toList()
				.blockingGet();

		final List<String> filteredAktoerIdListTilgangSak = filteredTilgangSakList.stream()
				.map(tilgangSak -> tilgangSak.getAktoerId())
				.collect(Collectors.toList());

		final List<TilgangBruker> finalTilgangBrukerList = filteredTilgangBrukerList.stream()
				.filter(tilgangBruker -> filteredAktoerIdListTilgangSak.contains(tilgangBruker.getAktoerId()))
				.collect(Collectors.toList());

		final List<TilgangJournalpost> tilgangJournalpostList = finalTilgangBrukerList.stream()
				.flatMap(tilgangBruker -> tilgangsmodellRepository.findTilgangJournalposter(
						tilgangBruker,
						filteredTilgangSakList,
						dokumentoversiktFagsakArguments.getFraDato(),
						dokumentoversiktFagsakArguments.getTema(),
						dokumentoversiktFagsakArguments.getJournalposttyper(),
						dokumentoversiktFagsakArguments.getJournalstatuser(),
						safRequestContext).stream())
				.collect(Collectors.toList());

		final List<TilgangJournalpost> filteredTilgangJournalpostList = Flowable.fromIterable(tilgangJournalpostList)
				.flatMap(tilgangJournalpost ->
						Flowable.just(tilgangJournalpost)
								.observeOn(Schedulers.io())
								.filter(tj -> pep4.hasAccess(tj, safRequestContext))
				).toList()
				.blockingGet();

		return visningsmodellRepository.findJournalposter(filteredTilgangJournalpostList.stream()
				.map(TilgangJournalpost::getJournalpostId)
				.collect(Collectors.toList()), safRequestContext);
	}

	@Override
	public List<DokumentInfo> findDokumenter(final Journalpost journalpost, final SafRequestContext safRequestContext) {
		// TODO MMA-1092 Pep4 for TilgangDokument her (er dette allerede er avklart i TilgangJournalpost så må context vite om dette)
		return journalpost.getDokumenter();
	}
}
