package no.nav.saf.tilgangskontroll.pep;

import org.junit.jupiter.api.Disabled;
import org.mockito.InjectMocks;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
 @Disabled
public class Pep3ImplTest extends AbstractPepTest {

	@InjectMocks
	private Pep3Impl pep3;

//	@Test
//	public void shouldPermitWhenTemaIsAllowedAndRelevanteTredjeparterIsSupplied() {
//		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));
//		when(oidcValidatorTool.validate(OIDC_TOKEN_PERSON_USER_TEST)).thenReturn(true);
//		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);
//
//		boolean hasAccess = pep3.hasAccess(TilgangSak.builder()
//				.aktoerId(AKTOER_ID)
//				.tema(TEST_TEMA)
//				.build(), new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST, oidcValidatorTool));
//
//		verify(abacService).evaluate(request.capture());
//		XacmlRequest capturedRequest = request.getValue();
//
//		assertTrue(hasAccess);
//
//		assertCommonXacmlRequestResources(capturedRequest);
//		assertEquals(AKTOER_ID, capturedRequest.getResources().get(3).getValue().toString());
//	}
//
//	@Test
//	public void shouldPermitWhenTemaIsAllowedAndFmrIdSupplied() {
//		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));
//		when(oidcValidatorTool.validate(OIDC_TOKEN_PERSON_USER_TEST)).thenReturn(true);
//		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);
//
//		boolean hasAccess = pep3.hasAccess(TilgangSak.builder()
//				.foedselsnummer(FNR)
//				.tema(TEST_TEMA)
//				.build(), new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST, oidcValidatorTool));
//
//		verify(abacService).evaluate(request.capture());
//		XacmlRequest capturedRequest = request.getValue();
//
//		assertTrue(hasAccess);
//
//		assertCommonXacmlRequestResources(capturedRequest);
//		assertEquals(FNR, capturedRequest.getResources().get(3).getValue().toString());
//	}
//
//	@Test
//	public void shouldPermitWhenOnlyOrgnummerSupplied() {
//		boolean hasAccess = pep3.hasAccess(TilgangSak.builder()
//				.orgnummer(ORGNR)
//				.tema(TEST_TEMA)
//				.build(), new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST, oidcValidatorTool));
//
//		assertTrue(hasAccess);
//	}
//
//	private void assertCommonXacmlRequestResources(XacmlRequest capturedRequest) {
//		assertEquals(new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST, oidcValidatorTool).getSecurityContext()
//				.getOidcTokenBody(), capturedRequest.getEnvironments().get(0).getValue().toString());
//		assertEquals(SAF, capturedRequest.getEnvironments().get(1).getValue().toString());
//
//		assertEquals(SAF, capturedRequest.getResources().get(0).getValue().toString());
//		assertEquals(RESOURCE_SAF_SAK_DOKUMENT, capturedRequest.getResources().get(1).getValue().toString());
//		assertEquals(TEST_TEMA, capturedRequest.getResources().get(2).getValue().toString());
//	}
//
//	@Test
//	public void shouldDenyWhenAbacDenies() {
//		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.DENY, null, null, null));
//		when(oidcValidatorTool.validate(OIDC_TOKEN_PERSON_USER_TEST)).thenReturn(true);
//		boolean hasAccess = pep3.hasAccess(TilgangSak.builder()
//				.tema("FAR")
//				.build(), new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST, oidcValidatorTool));
//
//		assertFalse(hasAccess);
//	}
//}
