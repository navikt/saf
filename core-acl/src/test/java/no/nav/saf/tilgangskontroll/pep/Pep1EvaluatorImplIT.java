package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;

import javax.inject.Inject;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */

public class Pep1EvaluatorImplIT extends AbstractPepEvaluatorIT {

	@Inject
	@Qualifier("pep1")
	private PepEvaluator<TilgangBruker> pep1;

	@Test
	public void foo() {
		System.out.println(OIDC_TOKEN_PERSON_USER_TEST + " : " + OIDC_TOKEN_SERVICE_USER_TEST);
//		pep1.hasAccess(TilgangBruker.builder().build(), "");
	}
}
