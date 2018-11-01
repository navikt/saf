package no.nav.saf.tilgangskontroll.abac.service.obligation;

import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Obligation;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import org.springframework.stereotype.Component;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class DefaultObligationStrategy implements ObligationStrategy {

	@Override
	public boolean isSupported(String attributeId) {
		return false;
	}

	@Override
	public void perform(Obligation attribute, XacmlRequest request, XacmlResponse response) {
		//TODO Add action
	}
}
