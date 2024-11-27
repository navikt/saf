package no.nav.saf.query.dokumentoversikt.journalstatus;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostMapper;
import no.nav.saf.anticorruptionlayer.joark.domain.ArkivsakMapper;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivJournalpost;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.PaginatedArkivJournalpost;
import no.nav.saf.domain.TilgangsmodellRepository;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.domain.visningsmodell.SideInfo;
import no.nav.saf.exceptions.UgyldigInputException;
import no.nav.saf.metrics.Monitor;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.Pep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static no.nav.saf.domain.kode.Journalstatus.FEILREGISTRERT;
import static no.nav.saf.domain.tilgangsmodell.BaseTilgangMapper.mapTilgangJournalpost;
import static no.nav.saf.util.MDCUtility.addMdcData;

@Slf4j
@Component
class DokumentoversiktJournalstatusQuery {

	// Hvis nye gyldige journalstatuser legges til må PEP1G, PEP2, PEP2D og PEP3 filtrering vurderes innført
	private static final EnumMap<Journalstatus, JournalStatusCode> GYLDIGE_JOURNALSTATUSER = new EnumMap<>(Journalstatus.class);

	static {
		GYLDIGE_JOURNALSTATUSER.put(JournalStatusCode.U.toSafJournalstatus(), JournalStatusCode.U);
		GYLDIGE_JOURNALSTATUSER.put(JournalStatusCode.UB.toSafJournalstatus(), JournalStatusCode.UB);
	}

	private final TilgangsmodellRepository tilgangsmodellRepository;
	private final Pep<TilgangJournalpost> pep4;
	private final Pep<TilgangDokumentInfo> pep5;
	private final Pep<TilgangDokumentvariant> pep6d;

	@Autowired
	public DokumentoversiktJournalstatusQuery(TilgangsmodellRepository tilgangsmodellRepository,
											  @Autowired Pep<TilgangJournalpost> pep4,
											  @Autowired Pep<TilgangDokumentInfo> pep5,
											  @Autowired Pep<TilgangDokumentvariant> pep6d) {
		this.tilgangsmodellRepository = tilgangsmodellRepository;
		this.pep4 = pep4;
		this.pep5 = pep5;
		this.pep6d = pep6d;
	}

	@Monitor(value = "dok_request", extraTags = {"process", "dokumentOversikt", "requestType", "journalstatus"}, histogram = true)
	public Dokumentoversikt hentDokumentoversikt(DokumentoversiktJournalstatusArguments dokumentoversiktJournalstatusArguments, SafRequestContext safRequestContext) {
		JournalStatusCode journalstatus = validateAndGetJournalstatus(dokumentoversiktJournalstatusArguments.getFilters().getJournalstatuser());

		Optional<PaginatedArkivJournalpost> paginatedArkivJournalpost = tilgangsmodellRepository.findTilgangJournalposterStatus(
				dokumentoversiktJournalstatusArguments.getFilters().getFraDato(),
				dokumentoversiktJournalstatusArguments.getFilters().getJournalposttyper(),
				journalstatus,
				dokumentoversiktJournalstatusArguments.getPagination().getFoerste(),
				dokumentoversiktJournalstatusArguments.getPagination().getEtterPeker());

		Map<Long, ArkivJournalpost> arkivJournalpostCache = new HashMap<>();

		final List<TilgangJournalpost> tilgangJournalpostList =
				paginatedArkivJournalpost.map(PaginatedArkivJournalpost::journalposter).orElse(emptyList())
						.stream()
						.map(journalpost -> {
							arkivJournalpostCache.put(journalpost.journalpostId(), journalpost);
							return mapTilgangJournalpost(journalpost);
						})
						.toList();

		final List<TilgangJournalpost> filteredTilgangJournalpostList = Flowable.fromIterable(tilgangJournalpostList)
				.parallel(10)
				.runOn(Schedulers.io())
				.doOnNext(tj -> addMdcData(safRequestContext))
				.filter(tj -> pep4.hasAccess(tj, safRequestContext))
				.doOnNext(tj -> tj.getDokumenter()
						.forEach(tilgangDokumentInfo -> pep5.hasAccess(tilgangDokumentInfo, safRequestContext)))
				.doOnNext(tj -> tj.getDokumenter()
						.forEach(tilgangDokumentInfo -> tilgangDokumentInfo.getTilgangDokumentvarianter()
								.forEach(tilgangDokumentvariant -> pep6d.hasAccess(tilgangDokumentvariant, safRequestContext))))
				.sequential()
				.toList()
				.blockingGet();

		// cache evt. saksinfo for bruk ved mapping til Journalpost
		filteredTilgangJournalpostList.stream()
				.map(tj -> arkivJournalpostCache.get(tj.getJournalpostId()))
				.filter(jp -> jp.saksrelasjon() != null)
				.map(ArkivsakMapper::mapArkivsak)
				.forEach(arkivsak -> safRequestContext.getRequestCache().putArkivsak(arkivsak));

		List<Journalpost> journalposter = filteredTilgangJournalpostList.stream()
				.map(TilgangJournalpost::getJournalpostId)
				.sorted(Comparator.reverseOrder())
				.map(arkivJournalpostCache::get)
				.map(arkivJournalpost ->
						ArkivJournalpostMapper.mapJournalpost(arkivJournalpost, safRequestContext.getRequestCache()))
				.filter(Objects::nonNull)
				.filter(j -> dokumentoversiktJournalstatusArguments.getFilters().getTema().contains(j.getTema()))
				.filter(j -> filterFeilregistrerte(dokumentoversiktJournalstatusArguments, j))
				.toList();

		return Dokumentoversikt.builder()
				.journalposter(journalposter)
				.sideInfo(paginatedArkivJournalpost
						.map(journalpostPagination -> new SideInfo(
								journalpostPagination.nextPage(),
								journalpostPagination.page() < journalpostPagination.totalPages(),
								journalposter.size(),
								journalpostPagination.totaltAntallRader()
						)).orElse(SideInfo.empty()))
				.build();
	}

	private JournalStatusCode validateAndGetJournalstatus(List<Journalstatus> journalstatuser) {
		Journalstatus journalstatus = journalstatuser.get(0); // vil alltid inneholde eksakt 1 journalstatus
		if (!GYLDIGE_JOURNALSTATUSER.containsKey(journalstatus)) {
			throw new UgyldigInputException(String.format("Ugyldig input: journalstatus=%s. journalstatus må være en av: %s.",
					journalstatus, GYLDIGE_JOURNALSTATUSER));
		}
		return GYLDIGE_JOURNALSTATUSER.get(journalstatus);
	}

	private boolean filterFeilregistrerte(DokumentoversiktJournalstatusArguments dokumentoversiktJournalstatusArguments, Journalpost j) {
		if (FEILREGISTRERT == j.getJournalstatus()) {
			return dokumentoversiktJournalstatusArguments.getFilters().isVisFeilregistrerte();
		}
		return true;
	}

}
