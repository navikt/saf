package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_DOKUMENT_METADATA;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_SKJERMING;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlAttribute;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.validation.OidcValidatorTool;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class Pep5ImplTest extends AbstractPepTest {

	@InjectMocks
	private Pep5Impl pep5;

	@Mock
	private OidcValidatorTool oidcValidatorTool;

	@Test
	void shouldPermitWhenSkjermingIsNotPresent() {
		when(oidcValidatorTool.validate(OIDC_TOKEN_PERSON_USER_TEST)).thenReturn(true);

		boolean hasAccess = pep5.hasAccess(TilgangDokumentInfo.builder()
				.skjerming(null)
				.build(), new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST, oidcValidatorTool));

		assertTrue(hasAccess);
	}

	@Test
	void shouldPermitWhenSkjermingIsPresentAndSaksbehandlerHasAccess() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));
		when(oidcValidatorTool.validate(OIDC_TOKEN_PERSON_USER_TEST)).thenReturn(true);

		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		boolean hasAccess = pep5.hasAccess(TilgangDokumentInfo.builder()
				.skjerming(SKJERMING_POL)
				.build(), new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST, oidcValidatorTool));

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertTrue(hasAccess);
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_DOKUMENT_METADATA)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_SAF_SKJERMING, SKJERMING_POL.name())));
	}

	@Test
	void shouldDenyWhenSkjermingIsPresentAndSaksbehandlerHasNotAccess() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.DENY, null, null, null));
		when(oidcValidatorTool.validate(OIDC_TOKEN_PERSON_USER_TEST)).thenReturn(true);

		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		boolean hasAccess = pep5.hasAccess(TilgangDokumentInfo.builder()
				.skjerming(SKJERMING_POL)
				.build(), new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST, oidcValidatorTool));

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertFalse(hasAccess);
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_DOKUMENT_METADATA)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_SAF_SKJERMING, SKJERMING_POL.name())));
	}

}
