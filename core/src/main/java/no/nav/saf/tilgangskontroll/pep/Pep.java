package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Policy Enforcement Point for ABAC.
 * <p>
 * Evaluerer tilgang til en ressurs T.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface Pep<T> {
	Logger logger = LoggerFactory.getLogger(Pep.class);

	XacmlResponse verifyAccessXacmlResponse(T ressurs, SafRequestContext safRequestContext);

	default boolean hasAccess(T ressurs, SafRequestContext safRequestContext) {
		XacmlResponse response = verifyAccessXacmlResponse(ressurs, safRequestContext);
		return Decision.PERMIT.equals(response.getDecision());
	}

	static void traceLogPepStarted(String pepName, Object ressurs) {
		if (logger.isTraceEnabled()) {
			logger.trace("{} evaluerer ressurs={}", pepName, ressurs.toString());
		}
	}

	static void traceLogPepFinished(String pepName, Object ressurs) {
		if (logger.isTraceEnabled()) {
			logger.trace("{} ferdig evaluert ressurs={}", pepName, ressurs.toString());
		}
	}

}
