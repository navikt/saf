package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.xacml.NavAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY;
import static no.nav.abac.xacml.NavAttributter.ENVIRONMENT_FELLES_PEP_ID;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_DOMENE;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_PERSON_FNR;
import static no.nav.saf.domain.DomainConstants.SAF;

import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Implementerer PEP1:
 * <p>
 * FP1 Behandling av Kode 6 Brukere
 * FP2 Behandling av Kode 7 Brukere
 * FP3 Egen ansatt
 * Saksbehandlers tilgang til enhet
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component("pep1")
public class Pep1EvaluatorImpl implements PepEvaluator<TilgangBruker> {

	private final AbacService abacService;

	@Inject
	public Pep1EvaluatorImpl(AbacService abacService) {
		this.abacService = abacService;
	}

	@Override
	public boolean hasAccess(TilgangBruker ressurs, SafRequestContext safRequestContext) {
		XacmlRequest request = new XacmlRequest();
		request.environment(ENVIRONMENT_FELLES_OIDC_TOKEN_BODY, safRequestContext.getOidcToken());
		request.environment(ENVIRONMENT_FELLES_PEP_ID, SAF);
		request.resource(RESOURCE_FELLES_DOMENE, SAF);
		request.resource(RESOURCE_FELLES_PERSON_FNR, ressurs.getFoedselsnr());

		XacmlResponse response = abacService.evaluate(request);
		return Decision.PERMIT.equals(response.getDecision());
	}
}
