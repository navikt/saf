package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_TEMA;
import static no.nav.saf.domain.DomainConstants.SAF;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class Pep2dEvaluatorTest extends AbstractPepTest {
	private static String AKTOER_ID = "1234";
	private static String TEST_TEMA = "BID";
	private static String OIDC_TOKEN_PERSON_USER_TEST = "Bearer " + "eyAidHlwIjogIkpXVCIsICJraWQiOiAiU0gxSWVSU2sxT1VGSDNzd1orRXVVcTE5VHZRPSIsICJhbGciOiAiUlMyNTYiIH0.eyAiYXRfaGFzaCI6ICJvNFUwMVhKNmlnRmw0VGYwdFRkYjR3IiwgInN1YiI6ICJaOTkwNDI0IiwgImF1ZGl0VHJhY2tpbmdJZCI6ICJlYTdmNWUxMi1jYjZjLTQ1ZjUtYmViMi0wYjVkYmI5ZDQ3YTItMTMzNzkzNCIsICJpc3MiOiAiaHR0cHM6Ly9pc3NvLXQuYWRlby5ubzo0NDMvaXNzby9vYXV0aDIiLCAidG9rZW5OYW1lIjogImlkX3Rva2VuIiwgImF1ZCI6ICJpZGEtdCIsICJjX2hhc2giOiAiRnJwNzhwdlJZU0VPMExjUktPUFdWdyIsICJvcmcuZm9yZ2Vyb2NrLm9wZW5pZGNvbm5lY3Qub3BzIjogIjJjYjQ2OGU4LThmMjItNGY1NS1hYTQ4LWM1NWExYjA4YmQ1ZiIsICJhenAiOiAiaWRhLXQiLCAiYXV0aF90aW1lIjogMTU0MzU3Nzk3MiwgInJlYWxtIjogIi8iLCAiZXhwIjogMTU0MzU4MTU3MiwgInRva2VuVHlwZSI6ICJKV1RUb2tlbiIsICJpYXQiOiAxNTQzNTc3OTcyIH0.NRgKaZhZ7qbBbJMUj_l9kzGOv7yOJVRVZDqmK0-G9lxzZs4jW1AtvFWqJRO9dd_djlIOGXz93UnuMNpWYWuoUd_S9gVc53yUjquzrys1IK8Zjd89smEl_9QP3ya8z7ISv48DciJORxdB2XT8rr2qpltYjKrCE2QmmK2ctAhy9QuFwEoZnctrR8IDKhUJCGd8LXPXddNRNEDL4-A47KwkF0UcfoDzPXznyZ2cbV4IkT3zvGqqwO3hovdrpadBdf204hClcmETYN3frRh1qHuTUqrBL7ualfqs-eDa4FKd77Mwu02LqPQGVpt8Ebebtv3OlS28YDchx8ng_P05okSjZg";

	@InjectMocks
	private Pep2dImpl pep2d;

	@Test
	public void pep2dHappyPath() {

		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));
		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		boolean hasAccess = pep2d.hasAccess(TilgangSak.builder()
				.tema(TEST_TEMA)
				.build(), new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST));

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertEquals(Boolean.TRUE, hasAccess);

		assertEquals(new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST).getSecurityContext().getOidcTokenBody(), capturedRequest.getEnvironments().get(0).getValue().toString());
		assertEquals(SAF, capturedRequest.getEnvironments().get(1).getValue().toString());

		assertEquals(SAF, capturedRequest.getResources().get(0).getValue().toString());
		assertEquals(RESOURCE_SAF_TEMA, capturedRequest.getResources().get(1).getValue().toString());

	}

	@Test
	public void pep2dDeny() {

		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.DENY, null, null, null));

		boolean hasAccess = pep2d.hasAccess(TilgangSak.builder()
				.tema("FAR")
				.build(), new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST));

		assertEquals(Boolean.FALSE, hasAccess);
	}

}
