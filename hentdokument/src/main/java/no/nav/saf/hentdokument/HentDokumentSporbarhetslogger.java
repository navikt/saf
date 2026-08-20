package no.nav.saf.hentdokument;

import lombok.extern.slf4j.Slf4j;
import no.nav.common.audit_log.cef.CefMessage;
import no.nav.common.audit_log.cef.CefMessageEvent;
import no.nav.common.audit_log.cef.CefMessageSeverity;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

import static no.nav.common.audit_log.cef.AuthorizationDecision.PERMIT;

/**
 * Sporingslogger til nais.audit.
 * https://confluence.adeo.no/display/BOA/saf+-+Sporingslogg+hentdokument
 */
@Slf4j
class HentDokumentSporbarhetslogger {
	private static final Logger auditlog = LoggerFactory.getLogger("hentdokument_sporbarhetslogg");

	private static final Tema UKJENT_TEMA = Tema.UKJ;
	private static final String UKJENT_BRUKERID = "ukjent";
	private static final String MACHINE_HENT_DOKUMENT_SAKSBEHANDLER = "hentdokument_saksbehandler";
	private static final String HENT_DOKUMENT_SAKSBEHANDLER = "Saksbehandler hentet dokument som gjelder bruker";

	void logPermit(String journalpostId, String dokumentInfoId, String variantFormat,
				   HentDokumentTilgang hentDokumentTilgang, SafRequestContext safRequestContext) {
		try {
			String journalpostTittel = hentDokumentTilgang.tilgangJournalpost().getJournalpostTittel();
			String requestingFagsystem = safRequestContext.getConsumerId();
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
					.name(HENT_DOKUMENT_SAKSBEHANDLER)
					.destinationUserId(getBrukerId(hentDokumentTilgang.tilgangBruker()))
					.sourceUserId(sourceUserId != null ? sourceUserId : "ukjent bruker")
					.authorizationDecision(PERMIT)
					.extension("act", MACHINE_HENT_DOKUMENT_SAKSBEHANDLER)
					.extension("shost", requestingFagsystem)
					.flexString(1, "journalpostId", journalpostId)
					.flexString(2, "dokumentInfoId", dokumentInfoId)
					.customString(3, "variantformat", variantFormat)
					.customString(5, "tittel", journalpostTittel != null ? journalpostTittel : "ukjent tittel")
					.customString(6, "tema", getTema(hentDokumentTilgang.tilgangSak()))
					.build();
			String messageString = message.toString();
			auditlog.info(messageString);
			log.info("saf hentdokument auditlog: {}", messageString);
		} catch (NullPointerException e) {
			log.error("saf hentdokument Unable to audit log for journalpostId={} dokumentInfoId={}, variantFormat={}", journalpostId, dokumentInfoId, variantFormat, e);
		}
	}

	private String getTema(TilgangSak tilgangSak) {
		if (tilgangSak == null) {
			return UKJENT_TEMA.name();
		} else {
			return tilgangSak.getTema() == null ? UKJENT_TEMA.name() : tilgangSak.getTema().name();
		}
	}

	private String getBrukerId(TilgangBruker tilgangBruker) {
		if (tilgangBruker != null) {
			if (tilgangBruker.isPerson() && tilgangBruker.getFoedselsnummer() != null) {
				return tilgangBruker.getFoedselsnummer();
			} else if (tilgangBruker.isOrganisasjon()) {
				return tilgangBruker.getOrgnummer();
			}
		}
		return UKJENT_BRUKERID;
	}
}
