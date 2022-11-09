package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.tilgangskontroll.SafRequestContext;

/**
 * Menneskelesbare grunner til deny fra PEP.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public final class DenyReasonFactory {
	private static final String DENY_PREFIX = "Tilgang til ressurs ble avvist. ";
	private static final String CONTACT_US_SUFFIX = " Hvis ikke dette fungerer, kontakt oss på #team_dokumentløsninger";
	private static final String PEP1G_DENY_REASON =
			" har ikke tilgang til ressurs tilhørende bruker som har kode 6/7, egen ansatt eller utenfor tillatt geografisk område.";

	private static final String PEP1G_DENY_SAKSBEHANDLER_INFO =
			"Saksbehandler må ha tilgang til Enhet som brukeren er tilknyttet i AXSYS.";
	private static final String PEP2_DENY_REASON =
			" har ikke tilgang til tema ressurset tilhører eller på grunn av Forvaltningsloven § 19.";

	private static final String PEP2D_DENY_SYSTEM_INFO = "System har ikke tilgang til tema ressurset tilhører. " +
			"\"tema_%s\" må ligge i feltet \"roles\" i Azure IAC-konfigurasjonen for konsumenten i saf sin nais-konfigurasjon.";
	private static final String PEP2D_DENY_SAKSBEHANDLER_INFO = "Saksbehandler har ikke tilgang til tema ressurset tilhører eller geografisk område. " +
			"Saksbehandler må ha tilgang til Enhet som brukeren er tilknyttet, med Fagområde=%s i AXSYS.";

	private static final String PEP3_DENY_REASON =
			" har ikke tilgang til ressurs der en av partene i bidragssaken har kode6/7 eller egen ansatt.";
	private static final String PEP4_DENY_REASON =
			" har ikke tilgang til ressurs på grunn av journalposten sin status.";
	private static final String PEP5_DENY_REASON =
			" har ikke tilgang til ressurs som er skjermet eller begrenset.";
	private static final String PEP6D_DENY_REASON =
			" har ikke tilgang til ressurs som er skjermet, begrenset eller logisk kassert.";
	private static final String PEP7D_DENY_REASON =
			" har ikke tilgang til ressurs relevante parter på sak har kode 6/7.";

	private DenyReasonFactory() {
		// noop
	}

	private static String saksbehandlerEllerSystem(boolean isSystem) {
		return isSystem ? "System" : "Saksbehandler";
	}
	public static String createPep1gDenyReason(SafRequestContext safRequestContext) {
		boolean isSystem = safRequestContext.getSecurityContext().isSystem();
		return DENY_PREFIX + saksbehandlerEllerSystem(isSystem) + PEP1G_DENY_REASON +
				(isSystem ? "" : PEP1G_DENY_SAKSBEHANDLER_INFO) + CONTACT_US_SUFFIX;
	}
	public static String createPep2DenyReason(SafRequestContext safRequestContext) {
		return DENY_PREFIX + saksbehandlerEllerSystem(safRequestContext.getSecurityContext().isSystem()) + PEP2_DENY_REASON + CONTACT_US_SUFFIX;
	}
	public static String createPep2dDenyReason(SafRequestContext safRequestContext, String tema) {
		return DENY_PREFIX + (safRequestContext.getSecurityContext().isSystem() ? PEP2D_DENY_SYSTEM_INFO.formatted(tema) : PEP2D_DENY_SAKSBEHANDLER_INFO.formatted(tema)) + CONTACT_US_SUFFIX;
	}
	public static String createPep3DenyReason(SafRequestContext safRequestContext) {
		return DENY_PREFIX + saksbehandlerEllerSystem(safRequestContext.getSecurityContext().isSystem()) + PEP3_DENY_REASON + CONTACT_US_SUFFIX;
	}
	public static String createPep4DenyReason(SafRequestContext safRequestContext) {
		return DENY_PREFIX + saksbehandlerEllerSystem(safRequestContext.getSecurityContext().isSystem()) + PEP4_DENY_REASON + CONTACT_US_SUFFIX;
	}
	public static String createPep5DenyReason(SafRequestContext safRequestContext) {
		return DENY_PREFIX + saksbehandlerEllerSystem(safRequestContext.getSecurityContext().isSystem()) + PEP5_DENY_REASON + CONTACT_US_SUFFIX;
	}
	public static String createPep6dDenyReason(SafRequestContext safRequestContext) {
		return DENY_PREFIX + saksbehandlerEllerSystem(safRequestContext.getSecurityContext().isSystem()) + PEP6D_DENY_REASON + CONTACT_US_SUFFIX;
	}
	public static String createPep7dDenyReason(SafRequestContext safRequestContext) {
		return DENY_PREFIX + saksbehandlerEllerSystem(safRequestContext.getSecurityContext().isSystem()) + PEP7D_DENY_REASON + CONTACT_US_SUFFIX;
	}
}