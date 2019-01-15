package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_SAK_JP_METADATA;
import static no.nav.saf.domain.DomainConstants.SAF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tjeneste.visningsmodell.kode.Tema;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class Pep2ImplTest extends AbstractPepTest {

	@InjectMocks
	private Pep2Impl pep2;

	@Test
	public void shouldPermitWhenTemaFarAccessIsPermitted() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));
		when(oidcValidatorTool.validate(OIDC_TOKEN_PERSON_USER_TEST)).thenReturn(true);

		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		boolean hasAccess = pep2.hasAccess(TilgangSak.builder()
				.tema(Tema.FAR.name())
				.build(), new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST, oidcValidatorTool));

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertTrue(hasAccess);

		assertEquals(new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST, oidcValidatorTool).getSecurityContext().getOidcTokenBody(), capturedRequest.getEnvironments().get(0).getValue().toString());
		assertEquals(SAF, capturedRequest.getEnvironments().get(1).getValue().toString());

		assertEquals(SAF, capturedRequest.getResources().get(0).getValue().toString());
		assertEquals(RESOURCE_SAF_SAK_JP_METADATA, capturedRequest.getResources().get(1).getValue().toString());
		assertEquals(Tema.FAR.name(), capturedRequest.getResources().get(2).getValue().toString());
	}

	@Test
	public void shouldDeny() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.DENY, null, null, null));

		boolean hasAccess = pep2.hasAccess(TilgangSak.builder()
				.tema(Tema.FAR.name())
				.build(), new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST, oidcValidatorTool));

		assertFalse(hasAccess);
	}
}
