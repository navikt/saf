package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component("pep4")
public class Pep4EvaluatorImpl implements PepEvaluator<TilgangJournalpost> {

	private final AbacService abacService;

	@Inject
	public Pep4EvaluatorImpl(AbacService abacService) {
		this.abacService = abacService;
	}

	@Override
	public boolean hasAccess(TilgangJournalpost ressurs, SafRequestContext safRequestContext) {
		XacmlRequest request = new XacmlRequest();
		//TODO Populate request and perform call to pdp

		return true;
	}
}
