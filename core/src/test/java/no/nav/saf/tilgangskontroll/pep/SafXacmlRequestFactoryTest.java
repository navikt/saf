package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.tilgangskontroll.SafSecurityContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlAttribute;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static no.nav.abac.common.xacml.CommonAttributter.ENVIRONMENT_FELLES_AZURE_JWT_TOKEN_BODY;
import static no.nav.abac.common.xacml.CommonAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY;
import static no.nav.abac.common.xacml.CommonAttributter.ENVIRONMENT_FELLES_PEP_ID;
import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_DOMENE;
import static no.nav.abac.common.xacml.CommonAttributter.XACML_1_0_ACTION_ACTION_ID;
import static no.nav.saf.domain.DomainConstants.SAF;
import static no.nav.saf.tilgangskontroll.pep.SafXacmlRequestFactory.ACTION_READ;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.mockito.Mockito.when;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@ExtendWith(MockitoExtension.class)
class SafXacmlRequestFactoryTest {

	private static final String NON_AZURE_TOKEN = "mytoken";
	private static final String AZURE_TOKEN = "myazuretoken";

	@Test
	void shouldPopulateDefaultXacmlRequest() {
		SafSecurityContext safSecurityContext = Mockito.mock(SafSecurityContext.class);
		when(safSecurityContext.getOidcTokenBody()).thenReturn(AZURE_TOKEN);
		when(safSecurityContext.isAzureToken()).thenReturn(false);
		XacmlRequest request = SafXacmlRequestFactory.create(safSecurityContext);

		assertThat(request.getEnvironments(), hasItem(new XacmlAttribute(ENVIRONMENT_FELLES_PEP_ID, SAF)));
		assertThat(request.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_DOMENE, SAF)));
		assertThat(request.getActions(), hasItem(new XacmlAttribute(XACML_1_0_ACTION_ACTION_ID, ACTION_READ)));
	}

	@Test
	void shouldPopulateFellesOidcTokenBodyWhenNotAzureToken() {
		SafSecurityContext safSecurityContext = Mockito.mock(SafSecurityContext.class);
		when(safSecurityContext.getOidcTokenBody()).thenReturn(NON_AZURE_TOKEN);
		when(safSecurityContext.isAzureToken()).thenReturn(false);
		XacmlRequest request = SafXacmlRequestFactory.create(safSecurityContext);

		assertThat(request.getEnvironments(), hasItem(new XacmlAttribute(ENVIRONMENT_FELLES_OIDC_TOKEN_BODY, NON_AZURE_TOKEN)));
	}

	@Test
	void shouldPopulateAzureTokenBodyWhenAzureToken() {
		SafSecurityContext safSecurityContext = Mockito.mock(SafSecurityContext.class);
		when(safSecurityContext.getOidcTokenBody()).thenReturn(AZURE_TOKEN);
		when(safSecurityContext.isAzureToken()).thenReturn(true);
		XacmlRequest request = SafXacmlRequestFactory.create(safSecurityContext);

		assertThat(request.getEnvironments(), hasItem(new XacmlAttribute(ENVIRONMENT_FELLES_AZURE_JWT_TOKEN_BODY, AZURE_TOKEN)));
	}
}