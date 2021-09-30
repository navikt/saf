package no.nav.saf.endpoints;

import java.util.UUID;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trim;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public final class HeaderUtils {
	private HeaderUtils() {
		// noop
	}

	public static String createNavCallid(final String navCallid, final String xCorrelationId) {
		if (isNotBlank(trim(navCallid))) {
			return trim(navCallid);
		}
		if (isNotBlank(trim(xCorrelationId))) {
			return trim(xCorrelationId);
		}
		// Fallback
		return UUID.randomUUID().toString();
	}
}
