package no.nav.saf.hentdokument;

import no.nav.saf.domain.HentDokument;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.exceptions.DokumentIkkeFunnetException;
import no.nav.saf.exceptions.HentdokumentTilgangskontrollException;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.AbacAnswer;
import no.nav.saf.tilgangskontroll.pep.Pep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static java.lang.String.format;
import static no.nav.saf.tilgangskontroll.pep.DenyReasonFactory.createPep1gDenyReason;
import static no.nav.saf.tilgangskontroll.pep.DenyReasonFactory.createPep2DenyReason;
import static no.nav.saf.tilgangskontroll.pep.DenyReasonFactory.createPep2dDenyReason;
import static no.nav.saf.tilgangskontroll.pep.DenyReasonFactory.createPep3DenyReason;
import static no.nav.saf.tilgangskontroll.pep.DenyReasonFactory.createPep4DenyReason;
import static no.nav.saf.tilgangskontroll.pep.DenyReasonFactory.createPep5DenyReason;
import static no.nav.saf.tilgangskontroll.pep.DenyReasonFactory.createPep6dDenyReason;
import static no.nav.saf.tilgangskontroll.pep.DenyReasonFactory.createPep7dDenyReason;

@Component
class HentDokumentDomainCoordinatorImpl implements HentDokumentDomainCoordinator {

	private final HentDokumentAntiCorruptionLayer hentDokumentAntiCorruptionLayer;
	private final HentDokumentTilgangService hentDokumentTilgangService;
	private final Pep<TilgangBruker> pep1g;
	private final Pep<TilgangSak> pep2;
	private final Pep<TilgangSak> pep2d;
	private final Pep<TilgangSak> pep3;
	private final Pep<TilgangJournalpost> pep4;
	private final Pep<TilgangDokumentInfo> pep5;
	private final Pep<TilgangDokumentvariant> pep6d;
	private final Pep<TilgangSak> pep7d;
	private final HentDokumentSporbarhetslogger hentDokumentSporbarhetslogger;

	@Autowired
	public HentDokumentDomainCoordinatorImpl(HentDokumentAntiCorruptionLayer hentDokumentAntiCorruptionLayer,
											 HentDokumentTilgangService hentDokumentTilgangService,
											 Pep<TilgangBruker> pep1g,
											 Pep<TilgangSak> pep2,
											 Pep<TilgangSak> pep2d,
											 Pep<TilgangSak> pep3,
											 Pep<TilgangJournalpost> pep4,
											 Pep<TilgangDokumentInfo> pep5,
											 Pep<TilgangDokumentvariant> pep6d,
											 Pep<TilgangSak> pep7d) {
		this.hentDokumentAntiCorruptionLayer = hentDokumentAntiCorruptionLayer;
		this.hentDokumentTilgangService = hentDokumentTilgangService;
		this.pep1g = pep1g;
		this.pep2 = pep2;
		this.pep2d = pep2d;
		this.pep3 = pep3;
		this.pep4 = pep4;
		this.pep5 = pep5;
		this.pep6d = pep6d;
		this.pep7d = pep7d;
		this.hentDokumentSporbarhetslogger = new HentDokumentSporbarhetslogger();
	}

	@Override
	public HentDokument hentDokument(final String journalpostId, final String dokumentInfoId, final String variantFormat, final SafRequestContext safRequestContext) {
		HentDokumentTilgang hentDokumentTilgang = hentDokumentTilgangService.hentDokumentTilgang(journalpostId, dokumentInfoId, variantFormat);
		if (hentDokumentTilgang.tilgangDokumentvariant().isEmpty()) {
			throw new DokumentIkkeFunnetException(format("Dokument med journalpostId=%s, dokumentInfoId=%s, variantFormat=%s ikke funnet i Joark.",
					journalpostId, dokumentInfoId, variantFormat));
		}

		doTilgangskontroll(hentDokumentTilgang, safRequestContext);
		hentDokumentSporbarhetslogger.logPermit(journalpostId, dokumentInfoId, variantFormat, hentDokumentTilgang, safRequestContext);
		return hentDokumentAntiCorruptionLayer.hentDokument(dokumentInfoId, variantFormat);
	}

	private void doTilgangskontroll(HentDokumentTilgang hentDokumentTilgang, SafRequestContext safRequestContext) {
		TilgangSak tilgangSak = hentDokumentTilgang.tilgangSak();
		TilgangJournalpost tilgangJournalpost = hentDokumentTilgang.tilgangJournalpost();
		AbacAnswer pep1gResponse = pep1g.hasAccessWithAnswer(hentDokumentTilgang.tilgangBruker(), safRequestContext);
		if (pep1gResponse.isDeny()) {
			throw new HentdokumentTilgangskontrollException(createPep1gDenyReason(safRequestContext, pep1gResponse), pep1gResponse);
		}

		AbacAnswer pep2Response = pep2.hasAccessWithAnswer(tilgangSak, safRequestContext);
		if (pep2Response.isDeny()) {
			throw new HentdokumentTilgangskontrollException(createPep2DenyReason(safRequestContext), pep2Response);
		}

		AbacAnswer pep3Response = pep3.hasAccessWithAnswer(tilgangSak, safRequestContext);
		if (pep3Response.isDeny()) {
			throw new HentdokumentTilgangskontrollException(createPep3DenyReason(safRequestContext), pep3Response);
		}

		if (tilgangJournalpost.getJournalstatus() != Journalstatus.MOTTATT) {
			AbacAnswer pep2dResponse = pep2d.hasAccessWithAnswer(tilgangSak, safRequestContext);
			if (pep2dResponse.isDeny()) {
				throw new HentdokumentTilgangskontrollException(createPep2dDenyReason(safRequestContext, tilgangSak), pep2dResponse);
			}
		}

		AbacAnswer pep4Response = pep4.hasAccessWithAnswer(tilgangJournalpost, safRequestContext);
		if (pep4Response.isDeny()) {
			throw new HentdokumentTilgangskontrollException(createPep4DenyReason(safRequestContext), pep4Response);
		}

		AbacAnswer pep5Response = pep5.hasAccessWithAnswer(hentDokumentTilgang.tilgangDokumentInfo(), safRequestContext);
		if (pep5Response.isDeny()) {
			throw new HentdokumentTilgangskontrollException(createPep5DenyReason(safRequestContext), pep5Response);
		}

		AbacAnswer pep6dResponse = pep6d.hasAccessWithAnswer(hentDokumentTilgang.tilgangDokumentvariant().orElse(null), safRequestContext);
		if (pep6dResponse.isDeny()) {
			throw new HentdokumentTilgangskontrollException(createPep6dDenyReason(safRequestContext), pep6dResponse);
		}

		AbacAnswer pep7dResponse = pep7d.hasAccessWithAnswer(tilgangSak, safRequestContext);
		if (pep7dResponse.isDeny()) {
			throw new HentdokumentTilgangskontrollException(createPep7dDenyReason(safRequestContext), pep7dResponse);
		}
	}
}
