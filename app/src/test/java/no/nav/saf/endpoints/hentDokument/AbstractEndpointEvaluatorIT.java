package no.nav.saf.endpoints.hentDokument;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static no.nav.saf.JwtClaimsBuilderProvider.openAmClaimsBuilder;

import com.auth0.jwt.JWT;
import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.freg.security.test.oidc.tools.OidcTestService;
import no.nav.freg.security.test.oidc.tools.TestToolsAutoConfig;
import no.nav.saf.ApplicationConfig;
import org.apache.cxf.helpers.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import java.io.IOException;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ApplicationConfig.class, TestToolsAutoConfig.class},
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("itest,wiremock,oidc")
@ImportAutoConfiguration
@AutoConfigureWireMock(port = 0)
public abstract class AbstractEndpointEvaluatorIT {

	static final String AKTOER_ID = "12345";
	static final String IDENTIFIKATOR = "***gammelt_fnr***";

	String OIDC_TOKEN_PERSON_USER_TEST;
	String OIDC_TOKEN_SERVICE_USER_TEST;
	private final String SERVICE_USER_ID = "srvsaf";
	private final String PERSON_USER_ID = "Z990782";

	@Inject
	protected OidcTestService oidcTestService;

	@Inject
	protected TestRestTemplate restTemplate;

	@BeforeEach
	public void setUp() {
		OIDC_TOKEN_PERSON_USER_TEST = "Bearer " + oidcTestService.createOidc(openAmClaimsBuilder().subject(PERSON_USER_ID)
				.build());
		OIDC_TOKEN_SERVICE_USER_TEST = "Bearer " + oidcTestService.createOidc(openAmClaimsBuilder().subject(SERVICE_USER_ID)
				.build());

		WireMock.reset();
		WireMock.resetAllRequests();
		WireMock.removeAllMappings();
	}

	protected String getOidcTokenBody(String oidcToken) {
		return JWT.decode(oidcToken).getPayload();
	}

	protected String stringFromClasspath(String resourcename) throws IOException {
		return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(resourcename));
	}

	protected void abacDeny() {
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));
	}

	protected void abacPermit() {
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected HttpEntity createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.TEXT_PLAIN);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_PERSON_USER_TEST);
		return new HttpEntity(headers);
	}
}
