package no.nav.saf.anticorruptionlayer;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public final class RetryConstants {
	//retries: 500ms, 1s sum = max vente-tid = 1.5s. maxAttempts inkluderer første forsøk
	public static final int MAX_ATTEMPTS_SHORT_PENSJON_V1 = 3;
	public static final int DELAY_SHORT_PENSJON_V1 = 500;
	public static final int MULTIPLIER_SHORT_PENSJON_V1 = 2;
	public static final int MAX_ATTEMPTS_SHORT_AKTOER_V2 = 3;

	public static final int DELAY_SHORT_AKTOER_V2 = 500;
	public static final int MULTIPLIER_SHORT_AKTOER_V2 = 2;

	private RetryConstants() {
		// ingen instansiering
	}
}
