package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.common.xacml.CommonAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY;
import static no.nav.abac.common.xacml.CommonAttributter.ENVIRONMENT_FELLES_PEP_ID;
import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_DOMENE;
import static no.nav.abac.common.xacml.CommonAttributter.XACML_1_0_ACTION_ACTION_ID;
import static no.nav.saf.domain.DomainConstants.SAF;
import static no.nav.saf.tilgangskontroll.pep.SafXacmlRequestFactory.ACTION_READ;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;

import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlAttribute;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import org.junit.jupiter.api.Test;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class SafXacmlRequestFactoryTest {

	private static final String MYTOKEN = "mytoken";

	@Test
	void shouldPopulateDefaultXacmlRequest() {
		XacmlRequest request = SafXacmlRequestFactory.create(MYTOKEN);

		assertThat(request.getEnvironments(), hasItem(new XacmlAttribute(ENVIRONMENT_FELLES_OIDC_TOKEN_BODY, MYTOKEN)));
		assertThat(request.getEnvironments(), hasItem(new XacmlAttribute(ENVIRONMENT_FELLES_PEP_ID, SAF)));
		assertThat(request.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_DOMENE, SAF)));
		assertThat(request.getActions(), hasItem(new XacmlAttribute(XACML_1_0_ACTION_ACTION_ID, ACTION_READ)));
	}
}