package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.tilgangskontroll.SafSecurityContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;

import static no.nav.saf.domain.DomainConstants.SAF;
import static no.nav.saf.tilgangskontroll.SafAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY;
import static no.nav.saf.tilgangskontroll.SafAttributter.ENVIRONMENT_FELLES_PEP_ID;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_DOMENE;
import static no.nav.saf.tilgangskontroll.SafAttributter.XACML_1_0_ACTION_ACTION_ID;

final class SafXacmlRequestFactory {

	static final String ACTION_READ = "read";

	private SafXacmlRequestFactory() {
		//Ingen instansiering
	}

	static XacmlRequest create(final SafSecurityContext safSecurityContext) {
		XacmlRequest request = new XacmlRequest();
		String cachedJwtPayload = safSecurityContext.getCachedJwtPayload();
		request.environment(ENVIRONMENT_FELLES_OIDC_TOKEN_BODY, cachedJwtPayload);
		request.environment(ENVIRONMENT_FELLES_PEP_ID, SAF);
		request.resource(RESOURCE_FELLES_DOMENE, SAF);
		request.action(XACML_1_0_ACTION_ACTION_ID, ACTION_READ);
		return request;
	}
}
