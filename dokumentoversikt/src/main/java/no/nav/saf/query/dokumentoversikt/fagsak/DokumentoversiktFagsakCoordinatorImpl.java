package no.nav.saf.query.dokumentoversikt.fagsak;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import no.nav.saf.domain.TilgangsmodellRepository;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.query.dokumentoversikt.DokumentoversiktVisningsmodellRepository;
import no.nav.saf.query.dokumentoversikt.SideInfoMapper;
import no.nav.saf.query.dokumentoversikt.arguments.DokumentoversiktPagination;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.Pep;
import no.nav.saf.tjeneste.argumenter.FagsakIdInput;
import no.nav.saf.tjeneste.visningsmodell.Dokumentoversikt;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import org.reactivestreams.Publisher;
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
class DokumentoversiktFagsakCoordinatorImpl implements DokumentoversiktFagsakCoordinator {

	private final SideInfoMapper sideInfoMapper = new SideInfoMapper();
	private final TilgangsmodellRepository tilgangsmodellRepository;
	private final DokumentoversiktVisningsmodellRepository visningsmodellRepository;
	private final Pep<TilgangBruker> pep1;
	private final Pep<TilgangSak> pep2;
	private final Pep<TilgangSak> pep2d;
	private final Pep<TilgangSak> pep3;
	private final Pep<TilgangJournalpost> pep4;

	@Inject
	public DokumentoversiktFagsakCoordinatorImpl(TilgangsmodellRepository tilgangsmodellRepository,
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
	public Dokumentoversikt hentDokumentoversikt(DokumentoversiktFagsakArguments dokumentoversiktFagsakArguments, SafRequestContext safRequestContext) {
		final FagsakIdInput fagsakIdInput = dokumentoversiktFagsakArguments.getFagsakIdInput();
		final List<TilgangBruker> tilgangBrukerList = tilgangsmodellRepository.findTilgangBrukerList(fagsakIdInput);

		List<TilgangBruker> filteredTilgangBrukerList = Flowable.fromIterable(tilgangBrukerList)
				.onErrorResumeNext((Function<Throwable, Publisher<? extends TilgangBruker>>) Flowable::error)
				.parallel(10)
				.runOn(Schedulers.io())
				.filter(ts -> pep1.hasAccess(ts, safRequestContext))
				.sequential()
				.toList()
				.blockingGet();

		final List<TilgangSak> tilgangSakList = tilgangsmodellRepository.findTilgangSaker(filteredTilgangBrukerList, fagsakIdInput, dokumentoversiktFagsakArguments
				.getFilters().getTema(), safRequestContext);

		final List<TilgangSak> filteredTilgangSakList = Flowable.fromIterable(tilgangSakList)
				.parallel(10)
				.runOn(Schedulers.io())
				.filter(ts -> pep2.hasAccess(ts, safRequestContext))
				.filter(ts -> pep2d.hasAccess(ts, safRequestContext))
				.filter(ts -> pep3.hasAccess(ts, safRequestContext))
				.sequential()
				.toList()
				.blockingGet();

		final List<TilgangJournalpost> tilgangJournalpostList = tilgangsmodellRepository.findTilgangJournalposter(
				new ArrayList<>(),
				filteredTilgangSakList,
				dokumentoversiktFagsakArguments.getFilters().getFraDato(),
				dokumentoversiktFagsakArguments.getFilters().getTema(),
				dokumentoversiktFagsakArguments.getFilters().getJournalposttyper(),
				dokumentoversiktFagsakArguments.getFilters().getJournalstatuser(),
				((DokumentoversiktPagination.SeekPagination) dokumentoversiktFagsakArguments.getPagination()).getFoerste(),
				((DokumentoversiktPagination.SeekPagination) dokumentoversiktFagsakArguments.getPagination()).getEtterPeker(),
				((DokumentoversiktPagination.SeekPagination) dokumentoversiktFagsakArguments.getPagination()).getSiste(),
				((DokumentoversiktPagination.SeekPagination) dokumentoversiktFagsakArguments.getPagination()).getFoerPeker(),
				safRequestContext);

		final List<TilgangJournalpost> filteredTilgangJournalpostList = Flowable.fromIterable(tilgangJournalpostList)
				.parallel(10)
				.runOn(Schedulers.io())
				.filter(tj -> pep4.hasAccess(tj, safRequestContext))
				.sequential()
				.toList()
				.blockingGet();

		List<Journalpost> journalposter = visningsmodellRepository.findJournalposter(filteredTilgangJournalpostList.stream()
				.map(TilgangJournalpost::getJournalpostId)
				.sorted(Comparator.reverseOrder())
				.collect(Collectors.toList()), safRequestContext);
		return Dokumentoversikt.builder()
				.journalposter(journalposter)
				.sideInfo(sideInfoMapper.mapSideInfo(journalposter, safRequestContext))
				.build();
	}

}
