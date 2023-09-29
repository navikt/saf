package no.nav.saf.hentdokument;

import no.nav.saf.domain.HentDokument;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.exceptions.HentdokumentTilgangskontrollException;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.AbacAnswer;
import no.nav.saf.tilgangskontroll.pep.Pep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
											 @Autowired Pep<TilgangBruker> pep1g,
											 @Autowired Pep<TilgangSak> pep2,
											 @Autowired Pep<TilgangSak> pep2d,
											 @Autowired Pep<TilgangSak> pep3,
											 @Autowired Pep<TilgangJournalpost> pep4,
											 @Autowired Pep<TilgangDokumentInfo> pep5,
											 @Autowired Pep<TilgangDokumentvariant> pep6d,
											 @Autowired Pep<TilgangSak> pep7d) {
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

		try {
			doTilgangskontroll(hentDokumentTilgang, safRequestContext);
			hentDokumentSporbarhetslogger.logPermit(journalpostId, dokumentInfoId, variantFormat, hentDokumentTilgang.tilgangSak(), hentDokumentTilgang.tilgangBruker(), safRequestContext);
			return hentDokumentAntiCorruptionLayer.hentDokument(dokumentInfoId, variantFormat);
		} catch (HentdokumentTilgangskontrollException e) {
			hentDokumentSporbarhetslogger.logDeny(journalpostId, dokumentInfoId, variantFormat, hentDokumentTilgang.tilgangSak(), hentDokumentTilgang.tilgangBruker(), safRequestContext, e);
			throw e;
		}
	}

	private void doTilgangskontroll(HentDokumentTilgang hentDokumentTilgang, SafRequestContext safRequestContext) {
		TilgangSak tilgangSak = hentDokumentTilgang.tilgangSak();
		TilgangJournalpost tilgangJournalpost = hentDokumentTilgang.tilgangJournalpost();
		AbacAnswer pep1gResponse = pep1g.hasAccessWithAnswer(hentDokumentTilgang.tilgangBruker(), safRequestContext);
		if (pep1gResponse.isDeny()) {
			throw new HentdokumentTilgangskontrollException(createPep1gDenyReason(safRequestContext), pep1gResponse.getDenyReasonSporing());
		}

		AbacAnswer pep2Response = pep2.hasAccessWithAnswer(tilgangSak, safRequestContext);
		if (pep2Response.isDeny()) {
			throw new HentdokumentTilgangskontrollException(createPep2DenyReason(safRequestContext), pep2Response.getDenyReasonSporing());
		}

		AbacAnswer pep3Response = pep3.hasAccessWithAnswer(tilgangSak, safRequestContext);
		if (pep3Response.isDeny()) {
			throw new HentdokumentTilgangskontrollException(createPep3DenyReason(safRequestContext), pep3Response.getDenyReasonSporing());
		}

		if (tilgangJournalpost.getJournalstatus() != Journalstatus.MOTTATT) {
			AbacAnswer pep2dResponse = pep2d.hasAccessWithAnswer(tilgangSak, safRequestContext);
			if (pep2dResponse.isDeny()) {
				throw new HentdokumentTilgangskontrollException(createPep2dDenyReason(safRequestContext, tilgangSak), pep2dResponse.getDenyReasonSporing());
			}
		}

		AbacAnswer pep4Response = pep4.hasAccessWithAnswer(tilgangJournalpost, safRequestContext);
		if (pep4Response.isDeny()) {
			throw new HentdokumentTilgangskontrollException(createPep4DenyReason(safRequestContext), pep4Response.getDenyReasonSporing());
		}

		AbacAnswer pep5Response = pep5.hasAccessWithAnswer(hentDokumentTilgang.tilgangDokumentInfo(), safRequestContext);
		if (pep5Response.isDeny()) {
			throw new HentdokumentTilgangskontrollException(createPep5DenyReason(safRequestContext), pep5Response.getDenyReasonSporing());
		}

		AbacAnswer pep6dResponse = pep6d.hasAccessWithAnswer(hentDokumentTilgang.tilgangDokumentvariant(), safRequestContext);
		if (pep6dResponse.isDeny()) {
			throw new HentdokumentTilgangskontrollException(createPep6dDenyReason(safRequestContext), pep6dResponse.getDenyReasonSporing());
		}

		AbacAnswer pep7dResponse = pep7d.hasAccessWithAnswer(tilgangSak, safRequestContext);
		if (pep7dResponse.isDeny()) {
			throw new HentdokumentTilgangskontrollException(createPep7dDenyReason(safRequestContext), pep7dResponse.getDenyReasonSporing());
		}
	}
}
