package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.xacml.NavAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY;
import static no.nav.abac.xacml.NavAttributter.ENVIRONMENT_FELLES_PEP_ID;
import static no.nav.abac.xacml.NavAttributter.RESOURCE_FELLES_DOMENE;
import static no.nav.saf.domain.DomainConstants.SAF;
import static org.mockito.Mockito.when;

import no.nav.saf.tilgangskontroll.abac.consumer.AbacConsumer;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.inject.Inject;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@ExtendWith(MockitoExtension.class)
public abstract class AbstractPepEvaluatorIT {

	@Inject
	public AbacConsumer abacConsumer;

	@Mock
	private AbacService abacService;

	protected void abacPermit() {
		XacmlRequest request = new XacmlRequest();
		request.environment(ENVIRONMENT_FELLES_OIDC_TOKEN_BODY, "oidcbody");
		request.environment(ENVIRONMENT_FELLES_PEP_ID, SAF);
		request.resource(RESOURCE_FELLES_DOMENE, SAF);

		//		XacmlRequest request = XacmlRequest.builder()
//				.resources()
//				.build();

//		resources = new ArrayList<>(xacmlRequest.getResources());
//		accessSubjects = new ArrayList<>(xacmlRequest.getAccessSubjects());
//		actions = new ArrayList<>(xacmlRequest.getActions());
//		environments = new ArrayList<>(xacmlRequest.getEnvironments());
//		failOnIndeterminate = xacmlRequest.failOnIndeterminate;
//		bias = xacmlRequest.getBias();

		// Unit test should go from
		// to AbacConsumer.evaluate
		// all dependencies in between should be mocked.

		when(abacConsumer.evaluate(request)).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));

//		stubFor(post(urlEqualTo("/abac"))
//				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
//						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//						.withBodyFile("abac/abac-permit.json")));
	}

	protected void abacDeny() {
		XacmlRequest request = new XacmlRequest();

		// Unit test should go from
		// to AbacConsumer.evaluate
		// all dependencies in between should be mocked.

		when(abacConsumer.evaluate(request)).thenReturn(new XacmlResponse(Decision.DENY, null, null, null));

//		stubFor(post(urlEqualTo("/abac"))
//				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
//						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//						.withBodyFile("abac/abac-deny.json")));
	}

}
