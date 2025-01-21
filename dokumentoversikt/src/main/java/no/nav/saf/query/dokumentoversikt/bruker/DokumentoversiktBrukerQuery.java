package no.nav.saf.query.dokumentoversikt.bruker;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.aktoer.PdlAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoMapper;
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
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.TilgangskontrollException;
import no.nav.saf.metrics.Monitor;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.AbacAnswer;
import no.nav.saf.tilgangskontroll.pep.Pep;
import no.nav.safselvbetjening.tilgang.Ident;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static no.nav.saf.graphql.ErrorCode.NOT_FOUND;
import static no.nav.saf.query.dokumentoversikt.SideInfoMapper.mapFilteredSideInfo;
import static no.nav.saf.tilgangskontroll.pep.DenyReasonFactory.createPep1gDenyReasonDokumentoversikt;
import static no.nav.saf.util.MDCUtility.addMdcData;

@Slf4j
@Component
class DokumentoversiktBrukerQuery {

	public static final String PERSON_IKKE_FUNNET_REASON = """
			Fant ikke bruker i Persondataløsningen (PDL). Kan derfor ikke slå opp dokumentoversikten til bruker. Hvis du ser dette i test, forsøk å gjenopprett brukeren i Dolly. Hvis det ikke hjelper, ta kontakt med #team_dokumentløsninger på slack""";
	private final DokumentoversiktBrukerTilgangsmodellRepository dokumentoversiktBrukerTilgangsmodellRepository;
	private final TilgangsmodellRepository tilgangsmodellRepository;
	private final Pep<TilgangBruker> pep1g;
	private final Pep<TilgangSak> pep2;
	private final Pep<TilgangSak> pep2d;
	private final Pep<TilgangSak> pep3;
	private final Pep<TilgangJournalpost> pep4;
	private final Pep<TilgangDokumentInfo> pep5;
	private final Pep<TilgangDokumentvariant> pep6d;
	private final Pep<TilgangSak> pep7d;
	private final JournalpostDtoMapper journalpostDtoMapper = new JournalpostDtoMapper();
	private final PdlAntiCorruptionLayer pdlAntiCorruptionLayer;

	@Autowired
	public DokumentoversiktBrukerQuery(DokumentoversiktBrukerTilgangsmodellRepository dokumentoversiktBrukerTilgangsmodellRepository,
									   TilgangsmodellRepository tilgangsmodellRepository,
									   Pep<TilgangBruker> pep1g,
									   Pep<TilgangSak> pep2,
									   Pep<TilgangSak> pep2d,
									   Pep<TilgangSak> pep3,
									   Pep<TilgangJournalpost> pep4,
									   Pep<TilgangDokumentInfo> pep5,
									   Pep<TilgangDokumentvariant> pep6d,
									   Pep<TilgangSak> pep7d,
									   PdlAntiCorruptionLayer pdlAntiCorruptionLayer
	) {
		this.dokumentoversiktBrukerTilgangsmodellRepository = dokumentoversiktBrukerTilgangsmodellRepository;
		this.tilgangsmodellRepository = tilgangsmodellRepository;
		this.pep1g = pep1g;
		this.pep2 = pep2;
		this.pep2d = pep2d;
		this.pep3 = pep3;
		this.pep4 = pep4;
		this.pep5 = pep5;
		this.pep6d = pep6d;
		this.pep7d = pep7d;
		this.pdlAntiCorruptionLayer = pdlAntiCorruptionLayer;
	}

	@Monitor(value = "dok_request", extraTags = {"process", "dokumentOversikt", "requestType", "bruker"}, histogram = true)
	public Dokumentoversikt hentDokumentoversikt(DokumentoversiktBrukerArguments dokumentoversiktBrukerArguments,
												 SafRequestContext safRequestContext) {
		TilgangBruker tilgangBruker = pdlAntiCorruptionLayer.findTilgangBruker(dokumentoversiktBrukerArguments.getBrukerIdInput());
		if (tilgangBruker == null) {
			throw new SafFunctionalException(PERSON_IKKE_FUNNET_REASON, NOT_FOUND);
		} else {
			safRequestContext.getRequestCache().putTilgangBruker(tilgangBruker);
		}

		AbacAnswer pep1gAnswer = this.pep1g.hasAccessWithAnswer(tilgangBruker, safRequestContext);
		if (pep1gAnswer.isDeny()) {
			throw new TilgangskontrollException(createPep1gDenyReasonDokumentoversikt(safRequestContext, pep1gAnswer), pep1gAnswer);
		}

		//  Resultat fra pep2d caches lokalt og brukes i JournalpostDtoMapper.java. Med bakgrunn i resultat fra pep2d, pep6d og pep7d settes feltet saksbehandlerHarTilgang=true/false.
		final Flowable<TilgangSak> tilgangSakFlow = dokumentoversiktBrukerTilgangsmodellRepository.findTilgangSaker(tilgangBruker, dokumentoversiktBrukerArguments
				.getFilters().getTema(), safRequestContext);
		List<TilgangSak> filteredTilgangSakList = tilgangSakFlow
				.onErrorResumeWith(Flowable.empty())
				.parallel(10)
				.runOn(Schedulers.io())
				.doOnNext(ts -> addMdcData(safRequestContext))
				.filter(ts -> pep2.hasAccess(ts, safRequestContext))
				.doOnNext(ts -> pep2d.hasAccess(ts, safRequestContext))
				.filter(ts -> pep3.hasAccess(ts, safRequestContext))
				.doOnNext(ts -> pep7d.hasAccess(ts, safRequestContext))
				.sequential()
				.toList().blockingGet();

		Map<Long, JournalpostDto> journalposter = tilgangsmodellRepository.findJournalposter(
				Collections.singletonList(tilgangBruker),
				filteredTilgangSakList,
				dokumentoversiktBrukerArguments.getFilters().getFraDato(),
				dokumentoversiktBrukerArguments.getFilters().getTilDato(),
				dokumentoversiktBrukerArguments.getFilters().getJournalposttyper(),
				dokumentoversiktBrukerArguments.getFilters().getJournalstatuser(),
				dokumentoversiktBrukerArguments.getPagination().getFoerste(),
				dokumentoversiktBrukerArguments.getPagination().getEtterPeker()
		);
		List<TilgangJournalpost> tilgangJournalposter = journalposter.values().stream()
				.map(TilgangsmodellRepository::mapTilgangJournalpost)
				.toList();

		// Pep2 og pep2d må utføres på midlertidige journalposter, da disse først dukker opp på bruker-søk i joark i forrige steg.
		final List<TilgangJournalpost> filteredTilgangJournalpostList = Flowable.fromIterable(tilgangJournalposter)
				.parallel(10)
				.runOn(Schedulers.io())
				.doOnNext(ts -> addMdcData(safRequestContext))
				.filter(tj -> pep4.hasAccess(tj, safRequestContext))
				.filter(tj -> pep2CheckMidlertidigAccess(tj, safRequestContext, journalposter))
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

		Set<Ident> brukerIdenter = tilgangBruker.getBrukersIdenterSomTilgangsIdenter().collect(Collectors.toSet());
		List<Journalpost> visningJournalposterSortert = filteredTilgangJournalpostList.stream()
				.map(TilgangJournalpost::getJournalpostId)
				.sorted(Comparator.reverseOrder())
				.map(journalposter::get)
				.map(journalpostDto ->
						journalpostDtoMapper.mapJournalpostDto(journalpostDto, brukerIdenter, safRequestContext.getRequestCache()))
				.filter(Objects::nonNull)
				.toList();

		List<Journalpost> visningJournalposterFiltrert = visningJournalposterSortert.stream()
				.filter(j -> dokumentoversiktBrukerArguments.getFilters().getTema().contains(j.getTema()))
				.filter(j -> filterFeilregistrerte(dokumentoversiktBrukerArguments, j))
				.toList();

		return Dokumentoversikt.builder()
				.journalposter(visningJournalposterFiltrert)
				.sideInfo(mapFilteredSideInfo(getLastJournalpostOnPage(journalposter, visningJournalposterSortert), visningJournalposterFiltrert))
				.build();
	}

	private static JournalpostDto getLastJournalpostOnPage(Map<Long, JournalpostDto> journalpostDtoMap, List<Journalpost> visningJournalposterSortert) {
		if (visningJournalposterSortert.isEmpty()) {
			return null;
		} else {
			String sisteJournalpostId = visningJournalposterSortert.getLast().getJournalpostId();
			return journalpostDtoMap.get(Long.parseLong(sisteJournalpostId));
		}
	}

	private boolean filterFeilregistrerte(DokumentoversiktBrukerArguments dokumentoversiktBrukerArguments, Journalpost j) {
		if (Journalstatus.FEILREGISTRERT == j.getJournalstatus()) {
			return dokumentoversiktBrukerArguments.getFilters().isVisFeilregistrerte();
		}
		return true;
	}

	private boolean pep2CheckMidlertidigAccess(TilgangJournalpost tj, SafRequestContext safRequestContext, Map<Long, JournalpostDto> journalpostDtoMap) {
		if (tj.getJournalstatus() == Journalstatus.MOTTATT) {
			TilgangSak midlertidigTilgangSak = mapToTilgangSak(journalpostDtoMap.get(tj.getJournalpostId()));
			return pep2.hasAccess(midlertidigTilgangSak, safRequestContext);
		} else {
			return true;
		}
	}

	private TilgangSak mapToTilgangSak(JournalpostDto journalpostDto) {
		return TilgangSak.builder()
				.tema(FagomradeCode.toSafTema(journalpostDto.getFagomrade()))
				.relevanteTredjeparter(new ArrayList<>())
				.build();
	}
}
