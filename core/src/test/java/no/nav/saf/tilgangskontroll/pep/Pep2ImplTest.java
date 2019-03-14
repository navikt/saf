package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_PARAGRAF19;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_SAK_JP_METADATA;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_TEMA;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlAttribute;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
class Pep2ImplTest extends AbstractPepTest {

	@InjectMocks
	private Pep2Impl pep2;

	@Test
	void shouldPermitWhenTemaFarAndParagraf19AccessIsPermitted() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));
		when(oidcValidatorTool.validate(OIDC_TOKEN_PERSON_USER_TEST)).thenReturn(true);

		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		boolean hasAccess = pep2.hasAccess(TilgangSak.builder()
				.tema(Tema.FAR)
				.paragraf19(true)
				.build(), new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST, xCorrelationID, oidcValidatorTool));

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertTrue(hasAccess);

		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_SAK_JP_METADATA)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_SAF_TEMA, Tema.FAR.name())));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_SAF_PARAGRAF19, true)));
	}

	@Test
	void shouldDenyWhenParagraf19IsNull() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.DENY, null, null, null));
		when(oidcValidatorTool.validate(OIDC_TOKEN_PERSON_USER_TEST)).thenReturn(true);

		boolean hasAccess = pep2.hasAccess(TilgangSak.builder()
				.tema(Tema.FAR)
				.paragraf19(null)
				.build(), new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST, xCorrelationID, oidcValidatorTool));

		assertFalse(hasAccess);
	}

	@Test
	void shouldDeny() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.DENY, null, null, null));
		when(oidcValidatorTool.validate(OIDC_TOKEN_PERSON_USER_TEST)).thenReturn(true);

		boolean hasAccess = pep2.hasAccess(TilgangSak.builder()
				.tema(Tema.FAR)
				.paragraf19(false)
				.build(), new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST, xCorrelationID, oidcValidatorTool));

		assertFalse(hasAccess);
	}
}
