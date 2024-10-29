package no.nav.saf.util;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.abbreviate;

public final class LogSanitizer {
	private static final String FNR_REGEX = "\\d{11}";
	private static final String FNR_MASKED = "***********";
	private static final int STRING_MAXLENGTH = 500;
	private static final Pattern EVERYTHING_EXCEPT_SAFE_CHARS_REGEX = Pattern.compile("[^a-zA-Z0-9]");

	public static String sanitizeFnr(final String message) {
		return message.replaceAll(FNR_REGEX, FNR_MASKED);
	}
	public static String removeUnsafeChars(String input) {
		if (input == null) {
			return null;
		}
		return abbreviate(EVERYTHING_EXCEPT_SAFE_CHARS_REGEX.matcher(input).replaceAll("_"), STRING_MAXLENGTH);
	}
}
