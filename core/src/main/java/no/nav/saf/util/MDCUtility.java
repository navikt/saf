package no.nav.saf.util;

import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.slf4j.MDC;

import java.util.UUID;

import static no.nav.saf.util.MDCConstants.CALL_ID;
import static no.nav.saf.util.MDCConstants.CONSUMER_ID;
import static no.nav.saf.util.MDCConstants.USER_ID;
import static org.apache.commons.lang3.StringUtils.isBlank;

public final class MDCUtility {
	private MDCUtility() {
		// noop
	}

	public static void addMdcData(SafRequestContext safRequestContext) {
		addMdcData(safRequestContext.getSecurityContext().getSubjectId(),
				safRequestContext.getSecurityContext().getNavCallid(),
				safRequestContext.getSecurityContext().getNavConsumerId());
	}

	public static void addMdcData(String userId, String callId, String consumerId) {
		MDC.put(USER_ID, userId);
		MDC.put(CALL_ID, callId);
		MDC.put(CONSUMER_ID, consumerId);
	}

	public static String getCallId() {
		final String callId = MDC.get(CALL_ID);
		return isBlank(callId) ? UUID.randomUUID().toString() : callId;
	}
}
