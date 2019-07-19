package no.nav.saf.endpoints;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
final class SafHeaders {
	@Deprecated // Bruk NAV_CALLID i stedet. Eksisterer for bakoverkompatibilitet.
	static final String X_CORRELATION_ID = "X-Correlation-ID";
	static final String NAV_CALLID = "Nav-Callid";
	static final String NAV_CONSUMER_ID = "Nav-Consumer-Id";

	private SafHeaders() {
		// noop
	}
}
