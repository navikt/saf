package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component("pep1b")
public class Pep1bEvaluatorImpl implements PepEvaluator<TilgangBruker> {

	private final AbacService abacService;

	@Inject
	public Pep1bEvaluatorImpl(AbacService abacService) {
		this.abacService = abacService;
	}

	@Override
	public boolean hasAccess(TilgangBruker ressurs, SafRequestContext safRequestContext) {
		XacmlRequest request = new XacmlRequest();
		//TODO Populate request and perform call to pdp

		return true;
	}
}
