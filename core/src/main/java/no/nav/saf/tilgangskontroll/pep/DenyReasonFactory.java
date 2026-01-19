package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;

/**
 * Menneskelesbare grunner til deny fra PEP.
 */
public final class DenyReasonFactory {
	private static final String DENY_PREFIX = "Tilgang til ressurs (journalpost/dokument) ble avvist. ";
	private static final String CONTACT_US_SUFFIX = " Hvis dette ikke fungerer; kontakt oss på #team_dokumentløsninger";
	public static final String PEP1G_DENY_REASON =
			" har ikke tilgang til ressurs tilhørende bruker som har kode 6/7 (strengt fortrolig/fortrolig adressesperre), egen ansatt eller utenfor tillatt geografisk område.";

	private static final String PEP1G_DENY_SAKSBEHANDLER_INFO =
			" Saksbehandler må ha tilgang til Enhet som brukeren er tilknyttet i AXSYS.";
	public static final String PEP2_DENY_REASON =
			" har ikke tilgang til tema ressursen tilhører eller på grunn av Forvaltningsloven § 19.";

	private static final String PEP2D_DENY_SYSTEM_INFO = "System har ikke tilgang til tema ressursen tilhører. " +
														 "\"dokument_tema_%s\" må ligge i feltet \"roles\" i Azure IAC-konfigurasjonen for konsumenten i saf sin nais-konfigurasjon.";
	private static final String PEP2D_DENY_SAKSBEHANDLER_INFO = "Saksbehandler har ikke tilgang til tema ressursen tilhører eller geografisk område. " +
																"Saksbehandler må ha tilgang til Enhet som brukeren er tilknyttet, med Fagområde=%s i AXSYS.";

	public static final String PEP3_DENY_REASON =
			" har ikke tilgang til ressurs der en av partene i bidragssaken har kode 6/7 (strengt fortrolig/fortrolig adressesperre) eller egen ansatt.";
	public static final String PEP4_DENY_REASON =
			" har ikke tilgang til ressurs på grunn av journalposten sin status.";
	public static final String PEP5_DENY_REASON =
			" har ikke tilgang til ressurs som er skjermet eller begrenset.";
	public static final String PEP6D_DENY_REASON =
			" har ikke tilgang til ressurs som er skjermet, begrenset eller logisk kassert.";
	public static final String PEP7D_DENY_REASON =
			" har ikke tilgang til ressurs der relevante parter på sak har kode 6/7 (strengt fortrolig/fortrolig adressesperre).";
	public static final String PEP8_DENY_REASON =
			" har ikke tilgang til ressurs som er tilknyttet en avsluttet sak.";

	private DenyReasonFactory() {
		// noop
	}

	private static String saksbehandlerEllerSystem(boolean isSystem) {
		return isSystem ? "System" : "Saksbehandler";
	}

	public static String createPep1gDenyReasonDokumentoversikt(SafRequestContext safRequestContext, PepAnswer pepAnswer) {
		boolean isSystem = safRequestContext.getSecurityContext().isSystem();
		String consumerType = saksbehandlerEllerSystem(isSystem);

		if (pepAnswer.getPepDenyReason().getDenyReasonCode() == DenyReasonCode.ORGNR_NAV_STAT) {
			return DENY_PREFIX +
				   "Dokumentoversikten er knyttet til organisasjon underlagt NAV og det krever egen ansatt behandling for oppslag på denne. " +
				   "NAV ansatt må være medlem av gruppen 0000-GA-Egne_ansatte";
		}
		return "Tilgang til dokumentoversikt ble avvist. " + consumerType + PEP1G_DENY_REASON +
			   (isSystem ? "" : PEP1G_DENY_SAKSBEHANDLER_INFO) + CONTACT_US_SUFFIX;
	}

	public static String createPep1gDenyReason(SafRequestContext safRequestContext, PepAnswer pepAnswer) {
		boolean isSystem = safRequestContext.getSecurityContext().isSystem();
		if (pepAnswer.getPepDenyReason().getDenyReasonCode() == DenyReasonCode.ORGNR_NAV_STAT) {
			return DENY_PREFIX +
				   "Journalpost/dokument er knyttet til organisasjon underlagt NAV og det krever egen ansatt behandling for oppslag på denne. " +
				   "NAV ansatt må være medlem av gruppen 0000-GA-Egne_ansatte";
		}
		return DENY_PREFIX + saksbehandlerEllerSystem(isSystem) + PEP1G_DENY_REASON +
			   (isSystem ? "" : PEP1G_DENY_SAKSBEHANDLER_INFO) + CONTACT_US_SUFFIX;
	}

	public static String createPep2DenyReason(SafRequestContext safRequestContext) {
		return DENY_PREFIX + saksbehandlerEllerSystem(safRequestContext.getSecurityContext().isSystem()) + PEP2_DENY_REASON;
	}

	public static String createPep2dDenyReason(SafRequestContext safRequestContext, TilgangSak tilgangSak) {
		if (tilgangSak == null) {
			return DENY_PREFIX + (safRequestContext.getSecurityContext().isSystem() ? PEP2D_DENY_SYSTEM_INFO.formatted("ukjent") : PEP2D_DENY_SAKSBEHANDLER_INFO.formatted("UKJENT")) + CONTACT_US_SUFFIX;
		}
		Tema tema = tilgangSak.getTema();
		return DENY_PREFIX + (safRequestContext.getSecurityContext().isSystem() ? PEP2D_DENY_SYSTEM_INFO.formatted(tema.name().toLowerCase()) : PEP2D_DENY_SAKSBEHANDLER_INFO.formatted(tema.name())) + CONTACT_US_SUFFIX;
	}

	public static String createPep3DenyReason(SafRequestContext safRequestContext) {
		return DENY_PREFIX + saksbehandlerEllerSystem(safRequestContext.getSecurityContext().isSystem()) + PEP3_DENY_REASON;
	}

	public static String createPep4DenyReason(SafRequestContext safRequestContext) {
		return DENY_PREFIX + saksbehandlerEllerSystem(safRequestContext.getSecurityContext().isSystem()) + PEP4_DENY_REASON;
	}

	public static String createPep5DenyReason(SafRequestContext safRequestContext) {
		return DENY_PREFIX + saksbehandlerEllerSystem(safRequestContext.getSecurityContext().isSystem()) + PEP5_DENY_REASON;
	}

	public static String createPep6dDenyReason(SafRequestContext safRequestContext) {
		return DENY_PREFIX + saksbehandlerEllerSystem(safRequestContext.getSecurityContext().isSystem()) + PEP6D_DENY_REASON;
	}

	public static String createPep7dDenyReason(SafRequestContext safRequestContext) {
		return DENY_PREFIX + saksbehandlerEllerSystem(safRequestContext.getSecurityContext().isSystem()) + PEP7D_DENY_REASON;
	}

	public static String createPep8DenyReason(SafRequestContext safRequestContext) {
		return DENY_PREFIX + saksbehandlerEllerSystem(safRequestContext.getSecurityContext().isSystem()) + PEP8_DENY_REASON;
	}
}