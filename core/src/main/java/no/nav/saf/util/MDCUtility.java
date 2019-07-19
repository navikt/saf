package no.nav.saf.util;

import static no.nav.saf.util.MDCConstants.CONSUMER_ID;
import static no.nav.saf.util.MDCConstants.CORRELATION_ID;
import static no.nav.saf.util.MDCConstants.USER_ID;

import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.slf4j.MDC;

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
		MDC.put(CORRELATION_ID, callId);
		MDC.put(CONSUMER_ID, consumerId);
	}
}
