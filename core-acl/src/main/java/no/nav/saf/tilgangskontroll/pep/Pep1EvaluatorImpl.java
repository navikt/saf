package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.xacml.NavAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY;
import static no.nav.abac.xacml.NavAttributter.ENVIRONMENT_FELLES_PEP_ID;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_PERSON_FNR;

import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.tilgangskontroll.AbacLogger;
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

	public static final String SAF = "saf";

	private final AbacService abacService;
	private final AbacLogger abacLogger;

	@Inject
	public Pep1EvaluatorImpl(AbacService abacService,
							 AbacLogger abacLogger) {
		this.abacService = abacService;
		this.abacLogger = abacLogger;
	}

	@Override
	public boolean hasAccess(TilgangBruker tilgangBruker, String oidcToken) {
		XacmlRequest request = new XacmlRequest();
		request.environment(ENVIRONMENT_FELLES_OIDC_TOKEN_BODY, oidcToken);
		request.environment(ENVIRONMENT_FELLES_PEP_ID, SAF);
		request.resource(ENVIRONMENT_FELLES_PEP_ID, SAF);
		request.resource(RESOURCE_FELLES_PERSON_FNR, tilgangBruker.getFoedselsnummer());

		XacmlResponse response = abacService.evaluate(request);
		return response.getDecision().getValue().equals(Decision.PERMIT);
	}

	private void setCommonAttributes(XacmlRequest request, String oidcToken) {
		request.environment(ENVIRONMENT_FELLES_OIDC_TOKEN_BODY, oidcToken);
		request.environment(ENVIRONMENT_FELLES_PEP_ID, SAF);
		request.resource(ENVIRONMENT_FELLES_PEP_ID, SAF);

	}

}

