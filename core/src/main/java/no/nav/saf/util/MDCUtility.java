package no.nav.saf.util;

import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.slf4j.MDC;

import java.util.UUID;

import static no.nav.saf.util.MDCConstants.CALL_ID;
import static no.nav.saf.util.MDCConstants.CONSUMER_ID;
import static no.nav.saf.util.MDCConstants.USER_ID;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Setter metadata på webtråden for å gjøre sporing av logger for kall enklere.
 */
public final class MDCUtility {
	private MDCUtility() {
		// noop
	}

	public static void addMdcData(SafRequestContext safRequestContext) {
		addMdcData(safRequestContext.getNavCallId(),
				safRequestContext.getUserId(),
				safRequestContext.getConsumerId());
	}

	public static void addMdcData(String callId, String userId, String consumerId) {
		MDC.put(CALL_ID, callId);
		MDC.put(USER_ID, userId);
		MDC.put(CONSUMER_ID, consumerId);
	}

	public static String getCallId() {
		final String callId = MDC.get(CALL_ID);
		return isBlank(callId) ? UUID.randomUUID().toString() : callId;
	}
}
