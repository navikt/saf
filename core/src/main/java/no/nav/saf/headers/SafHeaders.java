package no.nav.saf.headers;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public final class SafHeaders {
	/**
	 * @deprecated Bruk NAV_CALLID i stedet. Eksisterer for bakoverkompatibilitet.
	 */
	@Deprecated
	public static final String X_CORRELATION_ID = "X-Correlation-ID";
	public static final String NAV_CALLID = "Nav-Callid";
	public static final String NAV_CONSUMER_ID = "Nav-Consumer-Id";

	private SafHeaders() {
		// noop
	}
}
