package no.nav.saf.tilgangskontroll.abac.service.advice;

import static no.nav.saf.tilgangskontroll.abac.service.advice.AdviceTypes.ACTION_SPORBARHETSLOGG;

import no.nav.saf.tilgangskontroll.AbacLogger;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Advice;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class SprbarhetsLoggingAdviceStrategy implements AdviceStrategy {

	private final AbacLogger abacLogger;

	public SprbarhetsLoggingAdviceStrategy(@Qualifier("SporbarhetsLogger") AbacLogger abacLogger) {
		this.abacLogger = abacLogger;
	}

	@Override
	public boolean isSupported(String attributeId) {
		return ACTION_SPORBARHETSLOGG.equals(attributeId);
	}

	@Override
	public void perform(Advice attribute, XacmlRequest request, XacmlResponse response) {
		if (Decision.PERMIT.equals(response.getDecision())) {
			abacLogger.logAbacPermit(request, response);
		} else if (Decision.DENY.equals(response.getDecision())) {
			abacLogger.logAbacDeny(request, response);
		}
	}
}
