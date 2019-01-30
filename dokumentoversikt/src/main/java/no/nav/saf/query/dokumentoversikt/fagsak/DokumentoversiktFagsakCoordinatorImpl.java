package no.nav.saf.query.dokumentoversikt.fagsak;

import static no.nav.saf.domain.DomainConstants.PEP1G;
import static no.nav.saf.domain.DomainConstants.PEP2;
import static no.nav.saf.domain.DomainConstants.PEP2D;
import static no.nav.saf.domain.DomainConstants.PEP3;
import static no.nav.saf.domain.DomainConstants.PEP4;
import static no.nav.saf.domain.DomainConstants.PEP5;
import static no.nav.saf.domain.DomainConstants.PEP6D;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import no.nav.saf.domain.TilgangsmodellRepository;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.metrics.Monitor;
import no.nav.saf.query.dokumentoversikt.DokumentoversiktVisningsmodellRepository;
import no.nav.saf.query.dokumentoversikt.SideInfoMapper;
import no.nav.saf.query.dokumentoversikt.arguments.DokumentoversiktPagination;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.Pep;
import no.nav.saf.tjeneste.argumenter.FagsakInput;
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
	private final Pep<TilgangBruker> pep1g;
	private final Pep<TilgangSak> pep2;
	private final Pep<TilgangSak> pep2d;
	private final Pep<TilgangSak> pep3;
	private final Pep<TilgangJournalpost> pep4;
	private final Pep<TilgangDokumentInfo> pep5;
	private final Pep<TilgangDokumentvariant> pep6d;

	@Inject
	public DokumentoversiktFagsakCoordinatorImpl(TilgangsmodellRepository tilgangsmodellRepository,
												 DokumentoversiktVisningsmodellRepository visningsmodellRepository,
												 @Named(PEP1G) Pep<TilgangBruker> pep1g,
												 @Named(PEP2) Pep<TilgangSak> pep2,
												 @Named(PEP2D) Pep<TilgangSak> pep2d,
												 @Named(PEP3) Pep<TilgangSak> pep3,
												 @Named(PEP4) Pep<TilgangJournalpost> pep4,
												 @Named(PEP5) Pep<TilgangDokumentInfo> pep5,
												 @Named(PEP6D) Pep<TilgangDokumentvariant> pep6d) {
		this.tilgangsmodellRepository = tilgangsmodellRepository;
		this.visningsmodellRepository = visningsmodellRepository;
		this.pep1g = pep1g;
		this.pep2 = pep2;
		this.pep2d = pep2d;
		this.pep3 = pep3;
		this.pep4 = pep4;
		this.pep5 = pep5;
		this.pep6d = pep6d;
	}

	@Override
	@Monitor(value = "dok_request", extraTags = {"process", "dokumentOversikt", "requestType", "fagsak"}, histogram = true)
	public Dokumentoversikt hentDokumentoversikt(DokumentoversiktFagsakArguments dokumentoversiktFagsakArguments, SafRequestContext safRequestContext) {
		final FagsakInput fagsakInput = dokumentoversiktFagsakArguments.getFagsakInput();
		final List<TilgangBruker> tilgangBrukerList = tilgangsmodellRepository.findTilgangBrukerList(fagsakInput);

		List<TilgangBruker> filteredTilgangBrukerList = Flowable.fromIterable(tilgangBrukerList)
				.onErrorResumeNext((Function<Throwable, Publisher<? extends TilgangBruker>>) Flowable::error)
				.parallel(10)
				.runOn(Schedulers.io())
				.filter(ts -> pep1g.hasAccess(ts, safRequestContext))
				.sequential()
				.toList()
				.blockingGet();

		final List<TilgangSak> tilgangSakList = tilgangsmodellRepository.findTilgangSaker(filteredTilgangBrukerList, fagsakInput, dokumentoversiktFagsakArguments
				.getFilters().getTema(), safRequestContext);

		final List<TilgangSak> filteredTilgangSakList = Flowable.fromIterable(tilgangSakList)
				.parallel(10)
				.runOn(Schedulers.io())
				.filter(ts -> pep2.hasAccess(ts, safRequestContext))
				.doOnNext(ts -> pep2d.hasAccess(ts, safRequestContext))
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


		/**
		 * Resultat fra pep5 caches lokalt og brukes i JournalpostDtoMapper.java for å filtrere på dokumentinfo-metadata som skal gis til saksbehandler.
		 * Resultat fra pep6d caches også lokalt og brukes i JournalpostDtoMapper.java. Med bakgrunn i resultat fra pep2d og pep6d settes feltet saksbehandlerHarTilgang=true/false.
		 **/
		Flowable.fromIterable(filteredTilgangJournalpostList)
				.parallel(10)
				.runOn(Schedulers.io())
				.doOnNext(tj -> tj.getDokumenter()
						.forEach(tilgangDokumentInfo -> pep5.hasAccess(tilgangDokumentInfo, safRequestContext)))
				.doOnNext(tj -> tj.getDokumenter()
						.forEach(tilgangDokumentInfo -> tilgangDokumentInfo.getTilgangDokumentvarianter()
								.forEach(tilgangDokumentvariant -> pep6d.hasAccess(tilgangDokumentvariant, safRequestContext))));


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
