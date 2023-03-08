package no.nav.saf.headers;

public final class NavHeaders {
	/**
	 * @deprecated Bruk NAV_CALLID i stedet. Eksisterer for bakoverkompatibilitet.
	 */
	@Deprecated
	public static final String X_CORRELATION_ID = "X-Correlation-ID";
	public static final String NAV_CALLID = "Nav-Callid";
	public static final String NAV_USER_ID = "Nav-User-Id";
	// pdl
	public static final String PDL_NAV_CALLID = "Nav-Call-Id";
	private NavHeaders() {
		// noop
	}
}
