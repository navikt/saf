package no.nav.saf.tilgangskontroll.pep;

/**
 * Menneskelesbare grunner til deny fra PEP.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public final class DenyReasons {
	public static final String DENY_PREFIX = "Tilgang til ressurs ble avvist. ";
	public static final String CONTACT_US_SUFFIX = " Hvis ikke dette fungerer, kontakt oss på #team_dokumentløsninger";
	public static final String PEP1G_DENY_REASON = DENY_PREFIX +
			"Saksbehandler eller system har ikke tilgang til ressurs tilhørende bruker som har kode 6/7, egen ansatt eller utenfor tillatt geografisk område.";
	public static final String PEP2_DENY_REASON = DENY_PREFIX +
			"Saksbehandler eller system har ikke tilgang til tema ressurset tilhører eller på grunn av Forvaltningsloven § 19.";
	public static final String PEP2D_DENY_REASON = DENY_PREFIX +
			"Saksbehandler eller system har ikke tilgang til tema ressurset tilhører eller geografisk område. " +
			"Sjekk om bruker eller serviceuser har riktig gruppetilgang for tema. Det kan også skyldes at tema ikke er konfigurert i ABAC." +
			CONTACT_US_SUFFIX;
	public static final String PEP3_DENY_REASON = DENY_PREFIX +
			"Saksbehandler eller system har ikke tilgang til ressurs der en av partene i bidragssaken har kode6/7 eller egen ansatt.";
	public static final String PEP4_DENY_REASON = DENY_PREFIX +
			"Saksbehandler eller system har ikke tilgang til ressurs på grunn av journalposten sin status.";
	public static final String PEP5_DENY_REASON = DENY_PREFIX +
			"Saksbehandler eller system har ikke tilgang til ressurs som er skjermet eller begrenset.";
	public static final String PEP6D_DENY_REASON = DENY_PREFIX +
			"Saksbehandler eller system har ikke tilgang til ressurs som er skjermet, begrenset eller logisk kassert.";
	public static final String PEP7D_DENY_REASON = DENY_PREFIX +
			"Saksbehandler eller system har ikke tilgang til ressurs relevante parter på sak har kode 6/7.";

	private DenyReasons() {
		// noop
	}
}
