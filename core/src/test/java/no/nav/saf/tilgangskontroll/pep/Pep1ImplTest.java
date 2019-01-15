package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_PERSON;
import static no.nav.saf.domain.DomainConstants.SAF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangIdent;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.validation.OidcValidatorTool;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Collections;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class Pep1ImplTest extends AbstractPepTest {

	@InjectMocks
	private Pep1Impl pep1;

	@Mock
	private OidcValidatorTool oidcValidatorTool;

	@Test
	public void shouldPermitWhenAktoerIdIsEvaluated() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));
		when(oidcValidatorTool.validate(OIDC_TOKEN_PERSON_USER_TEST)).thenReturn(true);

		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		boolean hasAccess = pep1.hasAccess(TilgangBruker.builder()
				.aktoerId(AKTOER_ID)
				.foedselsnr(FNR)
				.historiskeIdenter(Collections.singletonList(TilgangIdent.builder().identifikator(FNR).build()))
				.build(), new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST, oidcValidatorTool));

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertTrue(hasAccess);

		assertCommonXacmlRequestResources(capturedRequest);
		assertEquals(AKTOER_ID, capturedRequest.getResources().get(2).getValue().toString());
	}

	@Test
	public void shouldPermitWhenFnrIsEvaluated() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));
		when(oidcValidatorTool.validate(OIDC_TOKEN_PERSON_USER_TEST)).thenReturn(true);
		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		boolean hasAccess = pep1.hasAccess(TilgangBruker.builder()
				.foedselsnr(FNR)
				.historiskeIdenter(Collections.singletonList(TilgangIdent.builder().identifikator(FNR).build()))
				.build(), new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST, oidcValidatorTool));

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();
		assertCommonXacmlRequestResources(capturedRequest);
		assertEquals(FNR, capturedRequest.getResources().get(2).getValue().toString());
		assertTrue(hasAccess);
	}

	private void assertCommonXacmlRequestResources(XacmlRequest capturedRequest) {
		assertEquals(new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST, oidcValidatorTool).getSecurityContext().getOidcTokenBody(), capturedRequest.getEnvironments().get(0).getValue().toString());
		assertEquals(SAF, capturedRequest.getEnvironments().get(1).getValue().toString());

		assertEquals(SAF, capturedRequest.getResources().get(0).getValue().toString());
		assertEquals(RESOURCE_SAF_PERSON, capturedRequest.getResources().get(1).getValue().toString());
	}

	@Test
	public void shouldDeny() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.DENY, null, null, null));

		boolean hasAccess = pep1.hasAccess(TilgangBruker.builder()
				.aktoerId(AKTOER_ID)
				.foedselsnr(FNR)
				.historiskeIdenter(Collections.singletonList(TilgangIdent.builder().identifikator(FNR).build()))
				.build(), new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST, oidcValidatorTool));

		assertFalse(hasAccess);
	}

}
