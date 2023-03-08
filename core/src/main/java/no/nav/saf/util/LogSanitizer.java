package no.nav.saf.util;

public final class LogSanitizer {
	private static final String FNR_REGEX = "\\d{11}";
	private static final String FNR_MASKED = "***********";

	public static String sanitizeFnr(final String message) {
		return message.replaceAll(FNR_REGEX, FNR_MASKED);
	}
}
