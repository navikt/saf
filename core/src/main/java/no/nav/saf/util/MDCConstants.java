package no.nav.saf.util;

public final class MDCConstants {
	/**
	 * callId logger en unik id inn til systemet for å kunne sammenstille loggmeldinger.
	 * Synonymer: correlationId, breadcrumbId
	 *
	 * @see no.nav.saf.tilgangskontroll.SafRequestContext
	 */
	public static final String CALL_ID = "callId";
	/**
	 * Brukeren (saksbehandler) som gjorde kallet. Forutsatt at kallet skjer i brukerkontekst.
	 *
	 * @see no.nav.saf.tilgangskontroll.SafRequestContext
	 */
	public static final String USER_ID = "userId";
	/**
	 * Systemet som gjorde kallet.
	 *
	 * @see no.nav.saf.tilgangskontroll.SafRequestContext
	 */
	public static final String CONSUMER_ID = "consumerId";

	/**
	 * JournalpostId det gjelder (journalpost query, hentdokument)
	 * <p>
	 * For sporing på abac logging
	 */
	public static final String JOURNALPOST_ID = "journalpostId";

	/**
	 * eksternReferanseId det gjelder (journalpost query)
	 * <p>
	 * For sporing på abac logging
	 */
	public static final String EKSTERNREFERANSE_ID = "eksternReferanseId";

	/**
	 * dokumentInfoId det gjelder (hentdokument)
	 * <p>
	 * For sporing på abac logging
	 */
	public static final String DOKUMENT_INFO_ID = "dokumentInfoId";

	private MDCConstants() {

	}
}
