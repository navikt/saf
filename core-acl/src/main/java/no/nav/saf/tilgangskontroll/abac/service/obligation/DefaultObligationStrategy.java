package no.nav.saf.tilgangskontroll.abac.service.obligation;

import no.nav.saf.tilgangskontroll.abac.dto.response.Obligation;
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
	public void perform(Obligation attribute) {
		//TODO Add action
	}
}
