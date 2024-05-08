package no.nav.saf.hentdokument;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.audit_log.cef.CefMessage;
import no.nav.common.audit_log.cef.CefMessageEvent;
import no.nav.common.audit_log.cef.CefMessageSeverity;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;

import java.time.Instant;

import static no.nav.common.audit_log.cef.AuthorizationDecision.PERMIT;

/**
 * Sporingslogger til nais.audit.
 * https://confluence.adeo.no/display/BOA/saf+-+Sporingslogg+hentdokument
 */
@Slf4j(topic = "hentdokument_sporbarhetslogg")
class HentDokumentSporbarhetslogger {
	private static final Tema UKJENT_TEMA = Tema.UKJ;
	private static final String UKJENT_BRUKERID = "ukjent";
	private static final String MACHINE_HENT_DOKUMENT_SYSTEM = "hentdokument_system";
	private static final String MACHINE_HENT_DOKUMENT_SAKSBEHANDLER = "hentdokument_saksbehandler";
	private static final String HENT_DOKUMENT_SYSTEM = "System hentet dokument som gjelder bruker";
	private static final String HENT_DOKUMENT_SAKSBEHANDLER = "Saksbehandler hentet dokument som gjelder bruker";

	void logPermit(String journalpostId, String dokumentInfoId, String variantFormat,
				   HentDokumentTilgang hentDokumentTilgang, SafRequestContext safRequestContext) {
		String journalpostTittel = hentDokumentTilgang.tilgangJournalpost().getJournalpostTittel();
		String sourceUserId = safRequestContext.getUserId();
		CefMessage message = CefMessage.builder()
				.version(0)
				.applicationName("joark")
				.loggerName("saf_hentdokument")
				.logFormatVersion("1.0")
				.event(CefMessageEvent.ACCESS)
				.severity(CefMessageSeverity.INFO)
				.timeEnded(Instant.now().toEpochMilli())
				.callId(safRequestContext.getNavCallId())
				.name(humanReadableAction(safRequestContext))
				.destinationUserId(getBrukerId(hentDokumentTilgang.tilgangBruker()))
				.sourceUserId(sourceUserId)
				.authorizationDecision(PERMIT)
				.extension("devicePayloadId", safRequestContext.getNavCallId())
				.extension("act", machineReadableAction(safRequestContext))
				.flexString(1, "journalpostId", journalpostId)
				.flexString(2, "dokumentInfoId", dokumentInfoId)
				.customString(3, "variantformat", variantFormat)
				.customString(5, "tittel", journalpostTittel != null ? journalpostTittel : "ukjent tittel")
				.customString(6, "tema", getTema(hentDokumentTilgang.tilgangSak()))
				.build();
		log.info(message.toString());
	}

	private String humanReadableAction(SafRequestContext safRequestContext) {
		return safRequestContext.getSecurityContext().isSystem() ? HENT_DOKUMENT_SYSTEM : HENT_DOKUMENT_SAKSBEHANDLER;
	}

	private String machineReadableAction(SafRequestContext safRequestContext) {
		return safRequestContext.getSecurityContext().isSystem() ? MACHINE_HENT_DOKUMENT_SYSTEM : MACHINE_HENT_DOKUMENT_SAKSBEHANDLER;
	}

	private String getTema(TilgangSak tilgangSak) {
		if (tilgangSak == null) {
			return UKJENT_TEMA.name();
		} else {
			return tilgangSak.getTema() == null ? UKJENT_TEMA.name() : tilgangSak.getTema().name();
		}
	}

	private String getBrukerId(TilgangBruker tilgangBruker) {
		if (tilgangBruker == null) {
			return UKJENT_BRUKERID;
		}
		if (tilgangBruker.isPerson()) {
			return tilgangBruker.getFoedselsnr();
		} else {
			return tilgangBruker.getOrgnummer();
		}
	}
}
