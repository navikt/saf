package no.nav.saf.util;

import static no.nav.saf.util.MDCConstants.CORRELATION_ID;
import static no.nav.saf.util.MDCConstants.USER_ID;

import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.slf4j.MDC;

public final class MDCUtility {

	private MDCUtility() {

	}

	public static void addMdcData(SafRequestContext safRequestContext) {
		addMdcData(safRequestContext.getSecurityContext().getSubjectId(), safRequestContext.getSecurityContext().getXCorrelationID());
	}

	public static void addMdcData(String userId, String callId) {
		MDC.put(USER_ID, userId);
		MDC.put(CORRELATION_ID, callId);
	}

}
