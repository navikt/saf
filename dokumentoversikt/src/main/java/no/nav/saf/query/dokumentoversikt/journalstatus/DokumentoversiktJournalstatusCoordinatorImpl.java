package no.nav.saf.query.dokumentoversikt.journalstatus;

import static no.nav.saf.domain.DomainConstants.PEP4;
import static no.nav.saf.domain.DomainConstants.PEP5;
import static no.nav.saf.domain.DomainConstants.PEP6D;
import static no.nav.saf.util.MDCUtility.addMdcData;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.SaksrelasjonDto;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.TilgangsmodellRepository;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.exceptions.UgyldigInputException;
import no.nav.saf.metrics.Monitor;
import no.nav.saf.query.dokumentoversikt.DokumentoversiktVisningsmodellRepository;
import no.nav.saf.query.dokumentoversikt.SideInfoMapper;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.Pep;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
class DokumentoversiktJournalstatusCoordinatorImpl implements DokumentoversiktJournalstatusCoordinator {

	// Hvis nye gyldige journalstatuser legges til må PEP1G, PEP2, PEP2D og PEP3 filtrering vurderes innført
	private static final EnumSet GYLDIGE_JOURNALSTATUSER = EnumSet.of(Journalstatus.UTGAAR, Journalstatus.UKJENT_BRUKER);

	private final SideInfoMapper sideInfoMapper = new SideInfoMapper();
	private final DokumentoversiktJournalstatusTilgangsmodellRepository dokumentoversiktJournalstatusTilgangsmodellRepository;
	private final TilgangsmodellRepository tilgangsmodellRepository;
	private final DokumentoversiktVisningsmodellRepository visningsmodellRepository;
	private final Pep<TilgangJournalpost> pep4;
	private final Pep<TilgangDokumentInfo> pep5;
	private final Pep<TilgangDokumentvariant> pep6d;

	@Inject
	public DokumentoversiktJournalstatusCoordinatorImpl(DokumentoversiktJournalstatusTilgangsmodellRepository dokumentoversiktJournalstatusTilgangsmodellRepository,
														TilgangsmodellRepository tilgangsmodellRepository,
														DokumentoversiktVisningsmodellRepository visningsmodellRepository,
														@Named(PEP4) Pep<TilgangJournalpost> pep4,
														@Named(PEP5) Pep<TilgangDokumentInfo> pep5,
														@Named(PEP6D) Pep<TilgangDokumentvariant> pep6d) {
		this.dokumentoversiktJournalstatusTilgangsmodellRepository = dokumentoversiktJournalstatusTilgangsmodellRepository;
		this.tilgangsmodellRepository = tilgangsmodellRepository;
		this.visningsmodellRepository = visningsmodellRepository;
		this.pep4 = pep4;
		this.pep5 = pep5;
		this.pep6d = pep6d;
	}

	@Override
	@Monitor(value = "dok_request", extraTags = {"process", "dokumentOversikt", "requestType", "journalstatus"}, histogram = true)
	public Dokumentoversikt hentDokumentoversikt(DokumentoversiktJournalstatusArguments dokumentoversiktJournalstatusArguments, SafRequestContext safRequestContext) {

		Journalstatus journalstatus = validateAndGetJournalstatus(dokumentoversiktJournalstatusArguments.getFilters().getJournalstatuser());

		final List<TilgangJournalpost> tilgangJournalpostList = tilgangsmodellRepository.findTilgangJournalposterStatus(
				dokumentoversiktJournalstatusArguments.getFilters().getFraDato(),
				dokumentoversiktJournalstatusArguments.getFilters().getJournalposttyper(),
				journalstatus,
				dokumentoversiktJournalstatusArguments.getPagination().getFoerste(),
				dokumentoversiktJournalstatusArguments.getPagination().getEtterPeker(),
				safRequestContext);

		String sluttJournalpostId = sluttJournalpostId(tilgangJournalpostList);

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
		filteredTilgangJournalpostList.forEach(tj -> mapOgCacheArkivsak(tj.getJournalpostId(), safRequestContext));

		List<Journalpost> journalposter = visningsmodellRepository.findJournalposter(filteredTilgangJournalpostList.stream()
				.map(TilgangJournalpost::getJournalpostId)
				.sorted(Comparator.reverseOrder())
				.collect(Collectors.toList()), safRequestContext)
				.stream()
				.filter(j -> dokumentoversiktJournalstatusArguments.getFilters().getTema().contains(j.getTema()))
				.filter(j -> filterFeilregistrerte(dokumentoversiktJournalstatusArguments, j))
				.collect(Collectors.toList());

		return Dokumentoversikt.builder()
				.journalposter(journalposter)
				.sideInfo(sideInfoMapper.mapFilteredSideInfo(sluttJournalpostId, safRequestContext))
				.build();
	}

	private Journalstatus validateAndGetJournalstatus(List<Journalstatus> journalstatuser) {
		Journalstatus journalstatus = journalstatuser.get(0); // vil alltid inneholde eksakt 1 journalstatus
		if (!GYLDIGE_JOURNALSTATUSER.contains(journalstatus)) {
			throw new UgyldigInputException(String.format("Ugyldig input: journalstatus=%s. journalstatus må være en av: %s.",
					journalstatus, GYLDIGE_JOURNALSTATUSER));
		}
		return journalstatus;
	}

	private String sluttJournalpostId(List<TilgangJournalpost> tilgangJournalposter) {
		return tilgangJournalposter.isEmpty() ? null : tilgangJournalposter.get(tilgangJournalposter.size() - 1).getJournalpostId();
	}

	private void mapOgCacheArkivsak(String journalpostId, SafRequestContext safRequestContext) {
		JournalpostDto journalpostDto = safRequestContext.getRequestCache().getObject(journalpostId);
		if (journalpostDto.getSaksrelasjon() == null) {
			return;
		}

		String aktoerId = null;
		String orgnummer = null;
		if (journalpostDto.getBruker() != null) {
			String brukerId = journalpostDto.getBruker().getBrukerId();
			if (journalpostDto.getBruker().isPerson()) {
				aktoerId = brukerId;
			} else {
				orgnummer = brukerId;
			}
		}

		SaksrelasjonDto saksrelasjonDto = journalpostDto.getSaksrelasjon();
		Arkivsak arkivsak = Arkivsak.builder()
				.arkivsaksnummer(saksrelasjonDto.getSakId())
				.fagsaksystem(saksrelasjonDto.getFagsystem().name())
				.arkivsaksystem(FagsystemCode.toSafArkivsaksystem(saksrelasjonDto.getFagsystem()))
				.aktoerId(aktoerId)
				.orgnummer(orgnummer)
				.tema(FagomradeCode.toSafTema(journalpostDto.getFagomrade()))
				.build();

		safRequestContext.getRequestCache().putObject(arkivsak.getKey(), arkivsak);
	}

	private boolean filterFeilregistrerte(DokumentoversiktJournalstatusArguments dokumentoversiktJournalstatusArguments, Journalpost j) {
		if (Journalstatus.FEILREGISTRERT == j.getJournalstatus()) {
			return dokumentoversiktJournalstatusArguments.getFilters().isVisFeilregistrerte();
		} else {
			return true;
		}
	}
}
