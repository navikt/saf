package no.nav.saf.query.dokumentoversikt.fagsak;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoMapper;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.domain.TilgangsmodellRepository;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.metrics.Monitor;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.Pep;
import no.nav.saf.tjeneste.argumenter.FagsakInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static no.nav.saf.domain.kode.Journalstatus.FEILREGISTRERT;
import static no.nav.saf.query.dokumentoversikt.SideInfoMapper.mapFilteredSideInfo;
import static no.nav.saf.util.MDCUtility.addMdcData;

@Component
class DokumentoversiktFagsakQuery {

	private final JournalpostDtoMapper journalpostDtoMapper = new JournalpostDtoMapper();
	private final DokumentoversiktFagsakTilgangsmodellRepository dokumentoversiktFagsakTilgangsmodellRepository;
	private final TilgangsmodellRepository tilgangsmodellRepository;
	private final Pep<TilgangBruker> pep1g;
	private final Pep<TilgangSak> pep2;
	private final Pep<TilgangSak> pep2d;
	private final Pep<TilgangSak> pep3;
	private final Pep<TilgangJournalpost> pep4;
	private final Pep<TilgangDokumentInfo> pep5;
	private final Pep<TilgangDokumentvariant> pep6d;
	private final Pep<TilgangSak> pep7d;

	@Autowired
	public DokumentoversiktFagsakQuery(
			DokumentoversiktFagsakTilgangsmodellRepository dokumentoversiktFagsakTilgangsmodellRepository,
			TilgangsmodellRepository tilgangsmodellRepository,
			@Autowired Pep<TilgangBruker> pep1g,
			@Autowired Pep<TilgangSak> pep2,
			@Autowired Pep<TilgangSak> pep2d,
			@Autowired Pep<TilgangSak> pep3,
			@Autowired Pep<TilgangJournalpost> pep4,
			@Autowired Pep<TilgangDokumentInfo> pep5,
			@Autowired Pep<TilgangDokumentvariant> pep6d,
			@Autowired Pep<TilgangSak> pep7d) {
		this.dokumentoversiktFagsakTilgangsmodellRepository = dokumentoversiktFagsakTilgangsmodellRepository;
		this.tilgangsmodellRepository = tilgangsmodellRepository;
		this.pep1g = pep1g;
		this.pep2 = pep2;
		this.pep2d = pep2d;
		this.pep3 = pep3;
		this.pep4 = pep4;
		this.pep5 = pep5;
		this.pep6d = pep6d;
		this.pep7d = pep7d;
	}

	@Monitor(value = "dok_request", extraTags = {"process", "dokumentOversikt", "requestType", "fagsak"}, histogram = true)
	public Dokumentoversikt hentDokumentoversikt(DokumentoversiktFagsakArguments dokumentoversiktFagsakArguments, SafRequestContext safRequestContext) {
		final FagsakInput fagsakInput = dokumentoversiktFagsakArguments.getFagsakInput();
		final List<TilgangBruker> tilgangBrukerList = dokumentoversiktFagsakTilgangsmodellRepository.findTilgangBrukerList(fagsakInput);

		List<TilgangBruker> filteredTilgangBrukerList = Flowable.fromIterable(tilgangBrukerList)
				.onErrorResumeNext(Flowable::error)
				.parallel(10)
				.runOn(Schedulers.io())
				.doOnNext(ts -> addMdcData(safRequestContext))
				.filter(ts -> pep1g.hasAccess(ts, safRequestContext))
				.sequential()
				.toList()
				.blockingGet();

		final List<TilgangSak> tilgangSakList = dokumentoversiktFagsakTilgangsmodellRepository.findTilgangSaker(filteredTilgangBrukerList, fagsakInput, dokumentoversiktFagsakArguments
				.getFilters().getTema(), safRequestContext);

		final List<TilgangSak> filteredTilgangSakList = Flowable.fromIterable(tilgangSakList)
				.parallel(10)
				.runOn(Schedulers.io())
				.doOnNext(ts -> addMdcData(safRequestContext))
				.filter(ts -> pep2.hasAccess(ts, safRequestContext))
				.doOnNext(ts -> pep2d.hasAccess(ts, safRequestContext))
				.filter(ts -> pep3.hasAccess(ts, safRequestContext))
				.doOnNext(ts -> pep7d.hasAccess(ts, safRequestContext))
				.sequential()
				.toList()
				.blockingGet();

		final Map<Long, JournalpostDto> journalposter = tilgangsmodellRepository.findJournalposter(
				new ArrayList<>(),
				filteredTilgangSakList,
				dokumentoversiktFagsakArguments.getFilters().getFraDato(),
				dokumentoversiktFagsakArguments.getFilters().getTilDato(),
				dokumentoversiktFagsakArguments.getFilters().getJournalposttyper(),
				dokumentoversiktFagsakArguments.getFilters().getJournalstatuser(),
				dokumentoversiktFagsakArguments.getPagination().getFoerste(),
				dokumentoversiktFagsakArguments.getPagination().getEtterPeker()
		);

		final List<TilgangJournalpost> tilgangJournalpostList = journalposter.values().stream()
				.map(TilgangsmodellRepository::mapTilgangJournalpost)
				.toList();

		final List<TilgangJournalpost> filteredTilgangJournalpostList = Flowable.fromIterable(tilgangJournalpostList)
				.parallel(10)
				.runOn(Schedulers.io())
				.filter(tj -> pep4.hasAccess(tj, safRequestContext))
				.sequential()
				.toList()
				.blockingGet();


		// Resultat fra pep5 caches lokalt og brukes i JournalpostDtoMapper.java for å filtrere på dokumentinfo-metadata som skal gis til saksbehandler.
		// Resultat fra pep6d caches også lokalt og brukes i JournalpostDtoMapper.java. Med bakgrunn i resultat fra pep2d og pep6d settes feltet saksbehandlerHarTilgang=true/false.
		Flowable.fromIterable(filteredTilgangJournalpostList)
				.parallel(10)
				.runOn(Schedulers.io())
				.doOnNext(tj -> tj.getDokumenter()
						.forEach(tilgangDokumentInfo -> pep5.hasAccess(tilgangDokumentInfo, safRequestContext)))
				.doOnNext(tj -> tj.getDokumenter()
						.forEach(tilgangDokumentInfo -> tilgangDokumentInfo.getTilgangDokumentvarianter()
								.forEach(tilgangDokumentvariant -> pep6d.hasAccess(tilgangDokumentvariant, safRequestContext))))
				.sequential()
				.toList()
				.blockingGet();


		List<Journalpost> visningJournalposterSortert = filteredTilgangJournalpostList.stream()
				.map(TilgangJournalpost::getJournalpostId)
				.sorted(Comparator.reverseOrder())
				.map(journalposter::get)
				.map(journalpostDto ->
						journalpostDtoMapper.mapJournalpostDto(journalpostDto, safRequestContext.getRequestCache()))
				.filter(Objects::nonNull)
				.toList();

		List<Journalpost> visningJournalposterFiltrert = visningJournalposterSortert.stream()
				.filter(j -> dokumentoversiktFagsakArguments.getFilters().getTema().contains(j.getTema()))
				.filter(j -> filterFeilregistrerte(dokumentoversiktFagsakArguments, j))
				.toList();

		return Dokumentoversikt.builder()
				.journalposter(visningJournalposterFiltrert)
				.sideInfo(mapFilteredSideInfo(getLastJournalpostOnPage(journalposter, visningJournalposterSortert), visningJournalposterSortert))
				.build();
	}

	private static JournalpostDto getLastJournalpostOnPage(Map<Long, JournalpostDto> safRequestContext, List<Journalpost> visningJournalposterSortert) {
		if (visningJournalposterSortert.isEmpty()) {
			return null;
		} else {
			String sisteJournalpostId = visningJournalposterSortert.getLast().getJournalpostId();
			return safRequestContext.get(Long.parseLong(sisteJournalpostId));
		}
	}

	private boolean filterFeilregistrerte(DokumentoversiktFagsakArguments dokumentoversiktFagsakArguments, Journalpost j) {
		if (FEILREGISTRERT.equals(j.getJournalstatus())) {
			return dokumentoversiktFagsakArguments.getFilters().isVisFeilregistrerte();
		}
		return true;
	}
}
