package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.common.xacml.CommonAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY;
import static no.nav.abac.common.xacml.CommonAttributter.ENVIRONMENT_FELLES_PEP_ID;
import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_DOMENE;
import static no.nav.saf.domain.DomainConstants.SAF;

import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
final class PepUtils {

	static void populateFellesAttributes(XacmlRequest request, String tokenBody) {
		request.environment(ENVIRONMENT_FELLES_OIDC_TOKEN_BODY, tokenBody);
		request.environment(ENVIRONMENT_FELLES_PEP_ID, SAF);
		request.resource(RESOURCE_FELLES_DOMENE, SAF);
	}

	private PepUtils() {
		//Ingen instansiering
	}
}
