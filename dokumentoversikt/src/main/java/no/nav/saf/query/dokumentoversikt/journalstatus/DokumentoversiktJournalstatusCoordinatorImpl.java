package no.nav.saf.query.dokumentoversikt.journalstatus;

import static no.nav.saf.domain.DomainConstants.PEP1G;
import static no.nav.saf.domain.DomainConstants.PEP2;
import static no.nav.saf.domain.DomainConstants.PEP2D;
import static no.nav.saf.domain.DomainConstants.PEP3;
import static no.nav.saf.domain.DomainConstants.PEP4;
import static no.nav.saf.domain.DomainConstants.PEP5;
import static no.nav.saf.domain.DomainConstants.PEP6D;
import static no.nav.saf.util.MDCUtility.addMdcData;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.TilgangsmodellRepository;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
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
	private final Pep<TilgangBruker> pep1g;
	private final Pep<TilgangSak> pep2;
	private final Pep<TilgangSak> pep2d;
	private final Pep<TilgangSak> pep3;
	private final Pep<TilgangJournalpost> pep4;
	private final Pep<TilgangDokumentInfo> pep5;
	private final Pep<TilgangDokumentvariant> pep6d;

	@Inject
	public DokumentoversiktJournalstatusCoordinatorImpl(DokumentoversiktJournalstatusTilgangsmodellRepository dokumentoversiktJournalstatusTilgangsmodellRepository,
														TilgangsmodellRepository tilgangsmodellRepository,
														DokumentoversiktVisningsmodellRepository visningsmodellRepository,
														@Named(PEP1G) Pep<TilgangBruker> pep1g,
														@Named(PEP2) Pep<TilgangSak> pep2,
														@Named(PEP2D) Pep<TilgangSak> pep2d,
														@Named(PEP3) Pep<TilgangSak> pep3,
														@Named(PEP4) Pep<TilgangJournalpost> pep4,
														@Named(PEP5) Pep<TilgangDokumentInfo> pep5,
														@Named(PEP6D) Pep<TilgangDokumentvariant> pep6d) {
		this.dokumentoversiktJournalstatusTilgangsmodellRepository = dokumentoversiktJournalstatusTilgangsmodellRepository;
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

		// TODO errorhandling?
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

		List<Journalpost> journalposter = visningsmodellRepository.findJournalposter(filteredTilgangJournalpostList.stream()
				.map(TilgangJournalpost::getJournalpostId)
				.sorted(Comparator.reverseOrder())
				.collect(Collectors.toList()), safRequestContext)
				.stream()
				.filter(j -> dokumentoversiktJournalstatusArguments.getFilters().getTema().contains(j.getTema()))
				.collect(Collectors.toList());
		return Dokumentoversikt.builder()
				.journalposter(journalposter)
				.sideInfo(sideInfoMapper.mapSideInfo(dokumentoversiktJournalstatusArguments.getPagination(), journalposter, safRequestContext))
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
}
