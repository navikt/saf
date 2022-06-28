package no.nav.saf.query.dokumentoversikt.bruker;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.domain.TilgangsmodellRepository;
import no.nav.saf.domain.kode.Journalstatus;
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
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.Pep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.saf.domain.DomainConstants.TILGANG_BRUKER;
import static no.nav.saf.util.MDCUtility.addMdcData;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
class DokumentoversiktBrukerQuery {

	private final SideInfoMapper sideInfoMapper = new SideInfoMapper();
	private final DokumentoversiktBrukerTilgangsmodellRepository dokumentoversiktBrukerTilgangsmodellRepository;
	private final TilgangsmodellRepository tilgangsmodellRepository;
	private final DokumentoversiktVisningsmodellRepository visningsmodellRepository;
	private final Pep<TilgangBruker> pep1g;
	private final Pep<TilgangSak> pep2;
	private final Pep<TilgangSak> pep2d;
	private final Pep<TilgangSak> pep3;
	private final Pep<TilgangJournalpost> pep4;
	private final Pep<TilgangDokumentInfo> pep5;
	private final Pep<TilgangDokumentvariant> pep6d;
	private final Pep<TilgangSak> pep7d;

	@Autowired
	public DokumentoversiktBrukerQuery(DokumentoversiktBrukerTilgangsmodellRepository dokumentoversiktBrukerTilgangsmodellRepository,
									   TilgangsmodellRepository tilgangsmodellRepository,
									   DokumentoversiktVisningsmodellRepository visningsmodellRepository,
									   @Autowired Pep<TilgangBruker> pep1g,
									   @Autowired Pep<TilgangSak> pep2,
									   @Autowired Pep<TilgangSak> pep2d,
									   @Autowired Pep<TilgangSak> pep3,
									   @Autowired Pep<TilgangJournalpost> pep4,
									   @Autowired Pep<TilgangDokumentInfo> pep5,
									   @Autowired Pep<TilgangDokumentvariant> pep6d,
									   @Autowired Pep<TilgangSak> pep7d
	) {
		this.dokumentoversiktBrukerTilgangsmodellRepository = dokumentoversiktBrukerTilgangsmodellRepository;
		this.tilgangsmodellRepository = tilgangsmodellRepository;
		this.visningsmodellRepository = visningsmodellRepository;
		this.pep1g = pep1g;
		this.pep2 = pep2;
		this.pep2d = pep2d;
		this.pep3 = pep3;
		this.pep4 = pep4;
		this.pep5 = pep5;
		this.pep6d = pep6d;
		this.pep7d = pep7d;
	}

	@Monitor(value = "dok_request", extraTags = {"process", "dokumentOversikt", "requestType", "bruker"}, histogram = true)
	public Dokumentoversikt hentDokumentoversikt(DokumentoversiktBrukerArguments dokumentoversiktBrukerArguments, SafRequestContext safRequestContext) {
		TilgangBruker tilgangBruker = dokumentoversiktBrukerTilgangsmodellRepository.findTilgangBruker(dokumentoversiktBrukerArguments.getBrukerIdInput());
		if (tilgangBruker != null) {
			safRequestContext.getRequestCache().putObject(TILGANG_BRUKER, tilgangBruker);
		}

		boolean pep1gAccess = this.pep1g.hasAccess(tilgangBruker, safRequestContext);

		if (!pep1gAccess) {
			return Dokumentoversikt.empty();
		}

		//  Resultat fra pep2d caches lokalt og brukes i JournalpostDtoMapper.java. Med bakgrunn i resultat fra pep2d, pep6d og pep7d settes feltet saksbehandlerHarTilgang=true/false.
		final Flowable<TilgangSak> tilgangSakFlow = dokumentoversiktBrukerTilgangsmodellRepository.findTilgangSaker(tilgangBruker, dokumentoversiktBrukerArguments
				.getFilters().getTema(), safRequestContext);
		List<TilgangSak> filteredTilgangSakList = tilgangSakFlow
				.onErrorResumeNext(Flowable.empty())
				.parallel(10)
				.runOn(Schedulers.io())
				.doOnNext(ts -> addMdcData(safRequestContext))
				.filter(ts -> pep2.hasAccess(ts, safRequestContext))
				.doOnNext(ts -> pep2d.hasAccess(ts, safRequestContext))
				.filter(ts -> pep3.hasAccess(ts, safRequestContext))
				.doOnNext(ts -> pep7d.hasAccess(ts, safRequestContext))
				.sequential()
				.toList().blockingGet();

		List<TilgangJournalpost> tilgangJournalposter = tilgangsmodellRepository.findTilgangJournalposter(
				Collections.singletonList(tilgangBruker),
				filteredTilgangSakList,
				dokumentoversiktBrukerArguments.getFilters().getFraDato(),
				dokumentoversiktBrukerArguments.getFilters().getTilDato(),
				dokumentoversiktBrukerArguments.getFilters().getJournalposttyper(),
				dokumentoversiktBrukerArguments.getFilters().getJournalstatuser(),
				dokumentoversiktBrukerArguments.getPagination().getFoerste(),
				dokumentoversiktBrukerArguments.getPagination().getEtterPeker(),
				safRequestContext
		);

		// Pep2 og pep2d må utføres på midlertidige journalposter, da disse først dukker opp på bruker-søk i joark i forrige steg.
		final List<TilgangJournalpost> filteredTilgangJournalpostList = Flowable.fromIterable(tilgangJournalposter)
				.parallel(10)
				.runOn(Schedulers.io())
				.doOnNext(ts -> addMdcData(safRequestContext))
				.filter(tj -> pep4.hasAccess(tj, safRequestContext))
				.filter(tj -> pep2CheckMidlertidigAccess(tj, safRequestContext))
				.sequential()
				.toList()
				.blockingGet();

		// Resultat fra pep5 caches lokalt og brukes i JournalpostDtoMapper.java for å filtrere på dokumentinfo-metadata som skal gis til saksbehandler.
		// Resultat fra pep6d og pep7d caches også lokalt og brukes i JournalpostDtoMapper.java. Med bakgrunn i resultat fra pep2d, pep6d og pep7d settes feltet saksbehandlerHarTilgang=true/false.
		Flowable.fromIterable(filteredTilgangJournalpostList)
				.parallel(10)
				.runOn(Schedulers.io())
				.doOnNext(ts -> addMdcData(safRequestContext))
				.doOnNext(tj -> tj.getDokumenter()
						.forEach(tilgangDokumentInfo -> pep5.hasAccess(tilgangDokumentInfo, safRequestContext)))
				.doOnNext(tj -> tj.getDokumenter()
						.forEach(tilgangDokumentInfo -> tilgangDokumentInfo.getTilgangDokumentvarianter()
								.forEach(tilgangDokumentvariant -> pep6d.hasAccess(tilgangDokumentvariant, safRequestContext))))
				.sequential()
				.toList()
				.blockingGet();

		List<Journalpost> visningJournalposterSortert = visningsmodellRepository.findJournalposter(filteredTilgangJournalpostList.stream()
				.map(TilgangJournalpost::getJournalpostId)
				.sorted(Comparator.reverseOrder())
				.collect(Collectors.toList()), safRequestContext);

		List<Journalpost> visningJournalposterFiltrert = visningJournalposterSortert.stream()
				.filter(j -> dokumentoversiktBrukerArguments.getFilters().getTema().contains(j.getTema()))
				.filter(j -> filterFeilregistrerte(dokumentoversiktBrukerArguments, j))
				.collect(Collectors.toList());

		return Dokumentoversikt.builder()
				.journalposter(visningJournalposterFiltrert)
				.sideInfo(sideInfoMapper.mapSideInfo(dokumentoversiktBrukerArguments.getPagination(), visningJournalposterSortert, safRequestContext))
				.build();
	}

	private boolean filterFeilregistrerte(DokumentoversiktBrukerArguments dokumentoversiktBrukerArguments, Journalpost j) {
		if (Journalstatus.FEILREGISTRERT == j.getJournalstatus()) {
			return dokumentoversiktBrukerArguments.getFilters().isVisFeilregistrerte();
		}
		return true;
	}

	private boolean pep2CheckMidlertidigAccess(TilgangJournalpost tj, SafRequestContext safRequestContext) {
		if (tj.getJournalstatus().equals(Journalstatus.MOTTATT)) {
			TilgangSak midlertidigTilgangSak = mapToTilgangSak(tj.getJournalpostId(), safRequestContext);
			return pep2.hasAccess(midlertidigTilgangSak, safRequestContext);
		} else {
			return true;
		}
	}

	private TilgangSak mapToTilgangSak(String journalpostId, SafRequestContext safRequestContext) {
		JournalpostDto journalpostDto = safRequestContext.getRequestCache().getObject(journalpostId);

		return TilgangSak.builder()
				.tema(FagomradeCode.toSafTema(journalpostDto.getFagomrade()))
				.relevanteTredjeparter(new ArrayList<>())
				.build();
	}
}
