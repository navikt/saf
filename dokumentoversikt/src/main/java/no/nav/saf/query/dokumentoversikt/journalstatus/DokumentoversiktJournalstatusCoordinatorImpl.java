package no.nav.saf.query.dokumentoversikt.journalstatus;

import static no.nav.saf.domain.DomainConstants.PEP1G;
import static no.nav.saf.domain.DomainConstants.PEP2;
import static no.nav.saf.domain.DomainConstants.PEP2D;
import static no.nav.saf.domain.DomainConstants.PEP3;
import static no.nav.saf.domain.DomainConstants.PEP4;
import static no.nav.saf.domain.DomainConstants.PEP5;
import static no.nav.saf.domain.DomainConstants.PEP6D;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.TilgangsmodellRepository;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.metrics.Monitor;
import no.nav.saf.query.dokumentoversikt.DokumentoversiktVisningsmodellRepository;
import no.nav.saf.query.dokumentoversikt.SideInfoMapper;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.Pep;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
class DokumentoversiktJournalstatusCoordinatorImpl implements DokumentoversiktJournalstatusCoordinator {

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
	@Monitor(value = "dok_request", extraTags = {"process", "dokumentOversikt", "requestType", "bruker"}, histogram = true)
	public Dokumentoversikt hentDokumentoversikt(DokumentoversiktJournalstatusArguments dokumentoversiktJournalstatusArguments, SafRequestContext safRequestContext) {

//		return Dokumentoversikt.builder()
//				.journalposter(journalposter)
//				.sideInfo(sideInfoMapper.mapSideInfo(dokumentoversiktFagsakArguments.getPagination(), journalposter, safRequestContext))
//				.build();
		return null;
	}
}
