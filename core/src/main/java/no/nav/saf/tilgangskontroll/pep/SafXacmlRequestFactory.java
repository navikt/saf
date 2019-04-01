package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.common.xacml.CommonAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY;
import static no.nav.abac.common.xacml.CommonAttributter.ENVIRONMENT_FELLES_PEP_ID;
import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_DOMENE;
import static no.nav.abac.common.xacml.CommonAttributter.XACML_1_0_ACTION_ACTION_ID;
import static no.nav.saf.domain.DomainConstants.SAF;

import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
final class SafXacmlRequestFactory {

	public static final String ACTION_READ = "read";

	private SafXacmlRequestFactory() {
		//Ingen instansiering
	}

	static XacmlRequest create(final String tokenBody) {
		XacmlRequest request = new XacmlRequest();
		request.environment(ENVIRONMENT_FELLES_OIDC_TOKEN_BODY, tokenBody);
		request.environment(ENVIRONMENT_FELLES_PEP_ID, SAF);
		request.resource(RESOURCE_FELLES_DOMENE, SAF);
		request.action(XACML_1_0_ACTION_ACTION_ID, ACTION_READ);

		return request;
	}
}
