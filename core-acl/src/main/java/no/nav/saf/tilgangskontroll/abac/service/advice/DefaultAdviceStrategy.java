package no.nav.saf.tilgangskontroll.abac.service.advice;

import no.nav.saf.tilgangskontroll.abac.dto.response.Advice;
import org.springframework.stereotype.Component;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class DefaultAdviceStrategy implements AdviceStrategy {

	@Override
	public boolean isSupported(String attributeId) {
		return false;
	}

	@Override
	public void perform(Advice attribute) {
		//TODO Add action
	}
}
