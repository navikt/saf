package no.nav.saf.endpoints;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.trim;

import java.util.UUID;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
abstract class AbstractSafController {
	String createNavCallid(final String navCallid, final String xCorrelationId) {
		if(isNotBlank(trim(navCallid))) {
			return trim(navCallid);
		}
		if(isNotBlank(trim(xCorrelationId))) {
			return trim(xCorrelationId);
		}
		// Fallback
		return UUID.randomUUID().toString();
	}
}
