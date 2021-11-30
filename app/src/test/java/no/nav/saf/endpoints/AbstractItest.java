package no.nav.saf.endpoints;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Options;
import lombok.SneakyThrows;
import no.nav.saf.ApplicationConfig;
import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.endpoints.testconfig.STSTestConfig;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.apache.cxf.helpers.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static java.util.Objects.requireNonNull;
import static org.apache.http.client.utils.URLEncodedUtils.CONTENT_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {AbstractItest.TestConfig.class, ApplicationConfig.class, STSTestConfig.class},
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles(value = {"itest", "wiremock"})
@EnableMockOAuth2Server
@AutoConfigureWireMock(port = Options.DYNAMIC_PORT)
public abstract class AbstractItest {
	private static final String SCENARIO_ABAC = "state_abac";
	private static final String STATE_PERMIT = "state_permit";
	private static final String STATE_PEP2 = "state_pep2";
	private static final String STATE_PEP2D = "state_pep2d";
	private static final String STATE_PEP3 = "state_pep3";
	private static final String STATE_PEP4 = "state_pep4";
	private static final String STATE_PEP5 = "state_pep5";
	private static final String STATE_PEP6D = "state_pep6d";
	private static final String STATE_PEP7D = "state_pep7d";

	@Configuration
	public static class TestConfig {
		@Bean
		@Primary
		ClientHttpRequestFactory clientHttpRequestFactoryTest() {
			return new SimpleClientHttpRequestFactory();
		}
	}

	@Autowired
	protected TestRestTemplate restTemplate;
	@Autowired
	private MockOAuth2Server server;

	@BeforeEach
	public void setUp() {
		WireMock.reset();
		WireMock.resetAllRequests();
		WireMock.removeAllMappings();
		WireMock.resetAllScenarios();
	}

	protected HttpEntity<?> createHttpEntity() {
		return new HttpEntity<>(createHeaders());
	}

	protected HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add(HttpHeaders.AUTHORIZATION, getHeaderToken());
		return headers;
	}

	private String getHeaderToken() {
		return "Bearer " + jwt("saksbehandler", new HashMap<>());
	}

	protected String jwt(String subject, Map<String, Object> claims) {
		String issuerId = "azurev2";
		String audience = "gosys";
		return server.issueToken(
				issuerId,
				"gosys-clientid",
				new DefaultOAuth2TokenCallback(
						issuerId,
						subject,
						List.of(audience),
						claims,
						60
				)
		).serialize();
	}

	protected void abacPermit() {
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected void abacDenyPep6dSkipPep3OrPep2() {
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP4));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP4)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP5));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP5)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP6D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP6D)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));
	}

	protected void abacDenyPep6dSkipPep2Pep3() {
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP4));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP4)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP5));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP5)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP6D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP6D)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));
	}

	protected void abacDenyPep6dSkipPep2Pep3Pep4Pep5() {
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP6D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP6D)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json"))
				.willSetStateTo(STATE_PERMIT));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PERMIT)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected void abacDenyPep5SkipPep2OrPep3() {
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP4));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP4)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP5));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP5)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));
	}

	protected void abacDenyPep5SkipPep2Pep3Pep4() {
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP5));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP5)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json"))
				.willSetStateTo(STATE_PERMIT));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PERMIT)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected void abacDenyPep5SkipPep1gPep2Pep2dPep3() {
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP5));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP5)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json"))
				.willSetStateTo(STATE_PERMIT));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PERMIT)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected void abacDenyPep4SkipPep1gPep2Pep2dPep3() {
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json"))
				.willSetStateTo(STATE_PERMIT));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PERMIT)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected void abacDenyPep4SkipPep2Pep3() {
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP4));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP4)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));
	}

	protected void abacDenyPep4SkipPep2OrPep3() {
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP4));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP4)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json"))
				.willSetStateTo(STATE_PERMIT));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PERMIT)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected void abacDenyPep3SkipPep2() {
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP3));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP3)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json"))
				.willSetStateTo(STATE_PERMIT));
	}

	protected void abacDenyPep3SkipPep2dAndPep2() {
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP3));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP3)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json"))
				.willSetStateTo(STATE_PERMIT));
	}

	protected void abacDenyPep2d() {
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json"))
				.willSetStateTo(STATE_PERMIT));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PERMIT)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected void abacDenyPep2dSkipPep2() {
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json"))
				.willSetStateTo(STATE_PERMIT));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PERMIT)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected void abacDenyPep2() {
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));
	}

	protected void abacDenyPep1g() {
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json"))
				.willSetStateTo(STATE_PERMIT));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PERMIT)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected void abacDenyPep7dSkipPep2Pep3Pep4Pep5Pep6() {
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP7D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP7D)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));
	}

	protected void verifyabacDenyPep7dSkipPep2Pep3Pep4Pep5Pep6AndHttpStatusCode(HttpStatus expectedHttpStatus, HttpStatus actualHttpStatus) {
		verify(2, postRequestedFor(urlEqualTo("/abac")));
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep1gAndHttpStatusCode(HttpStatus expectedHttpStatus, HttpStatus actualHttpStatus) {
		verify(1, postRequestedFor(urlEqualTo("/abac")));
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep2AndHttpStatusCode(HttpStatus expectedHttpStatus, HttpStatus actualHttpStatus) {
		verify(2, postRequestedFor(urlEqualTo("/abac")));
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep2dAndHttpStatusCode(boolean isDokumentoversikt, HttpStatus expectedHttpStatus, HttpStatus actualHttpStatus) {
		if (isDokumentoversikt) {
			verify(6, postRequestedFor(urlEqualTo("/abac")));
		} else {
			verify(3, postRequestedFor(urlEqualTo("/abac")));
		}
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep3ASkipPep2AndHttpStatusCode(HttpStatus expectedHttpStatus, HttpStatus actualHttpStatus) {
		verify(3, postRequestedFor(urlEqualTo("/abac")));
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep3SkipPep2AndPep2dAndHttpStatusCode(HttpStatus expectedHttpStatus, HttpStatus actualHttpStatus) {
		verify(2, postRequestedFor(urlEqualTo("/abac")));
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep4SkipPep2OrPep3AndHttpStatusCode(HttpStatus expectedHttpStatus, HttpStatus actualHttpStatus) {
		verify(4, postRequestedFor(urlEqualTo("/abac")));
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep4SkipPep2Pep3AndHttpStatusCode(HttpStatus expectedHttpStatus, HttpStatus actualHttpStatus) {
		verify(3, postRequestedFor(urlEqualTo("/abac")));
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep5SkipPep2OrPep3AndHttpStatusCode(boolean isDokumentoversikt, HttpStatus expectedHttpStatus, HttpStatus actualHttpStatus) {
		if (isDokumentoversikt) {
			verify(6, postRequestedFor(urlEqualTo("/abac")));
		} else {
			verify(5, postRequestedFor(urlEqualTo("/abac")));
		}
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep6dSkipPep2AndHttpStatusCode(HttpStatus expectedHttpStatus, HttpStatus actualHttpStatus) {
		verify(6, postRequestedFor(urlEqualTo("/abac")));
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep6dSkipPep2Pep3AndHttpStatusCode(HttpStatus expectedHttpStatus, HttpStatus actualHttpStatus) {
		verify(5, postRequestedFor(urlEqualTo("/abac")));
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyEmptyJournalpostListeAndNullSideInfo(Dokumentoversikt dokumentoversikt) {
		assertEquals(0, dokumentoversikt.getJournalposter().size());
		assertNull(dokumentoversikt.getSideInfo());
	}

	protected void verifyEmptyJournalpostListeAndEmptySideInfo(Dokumentoversikt dokumentoversikt) {
		assertEquals(0, dokumentoversikt.getJournalposter().size());
		assertNull(dokumentoversikt.getSideInfo().getSluttpeker());
		assertFalse(dokumentoversikt.getSideInfo().isFinnesNesteSide());
	}

	protected void assertSaksbehandlerHarTilgang(Dokumentoversikt dokumentoversikt) {
		dokumentoversikt.getJournalposter().forEach(
				journalpost -> journalpost.getDokumenter().forEach(
						dokumentInfo -> dokumentInfo.getDokumentvarianter().forEach(
								dokumentvariant -> assertTrue(dokumentvariant.isSaksbehandlerHarTilgang())))
		);
	}

	protected void assertSaksbehandlerHarIkkeTilgang(Dokumentoversikt dokumentoversikt) {
		dokumentoversikt.getJournalposter().forEach(
				journalpost -> journalpost.getDokumenter().forEach(
						dokumentInfo -> dokumentInfo.getDokumentvarianter().forEach(
								dokumentvariant -> assertFalse(dokumentvariant.isSaksbehandlerHarTilgang())))
		);
	}

	protected String base64(String journalpostId) {
		if (journalpostId == null) {
			return null;
		}
		return Base64.getEncoder().encodeToString(journalpostId.getBytes());
	}

	@SneakyThrows
	protected String stringFromClasspath(String resourcename) {
		return IOUtils.toString(requireNonNull(this.getClass().getClassLoader().getResourceAsStream(resourcename)));
	}
}
