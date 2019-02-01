package no.nav.saf.endpoints;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import no.nav.modig.testcertificates.TestCertificates;
import no.nav.saf.ApplicationConfig;
import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.endpoints.testconfig.STSTestConfig;
import no.nav.saf.exceptions.OidcAuthorizationException;
import org.apache.cxf.helpers.IOUtils;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.BeforeAll;
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
import java.time.LocalDateTime;


/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ApplicationConfig.class, STSTestConfig.class},
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("itest,wiremock")
@ImportAutoConfiguration
@AutoConfigureWireMock(port = 0)
public abstract class AbstractItest {

	private static String SCENARIO_ABAC = "state_abac";
	private static String STATE_PEP1G = "state_pep1g";
	private static String STATE_PEP2 = "state_pep2";
	private static String STATE_PEP2D = "state_pep2g";
	private static String STATE_PEP3 = "state_pep3";
	private static String STATE_PEP4 = "state_pep4";
	private static String STATE_PEP5 = "state_pep5";
	private static String STATE_PEP6D = "state_pe6g";
	public static final String NAV_STS_ISSUER_URL = "http://navStsIssuerUrl";
	protected static String OIDC_TOKEN_PERSON_USER_TEST;

	@Inject
	protected TestRestTemplate restTemplate;

	@Inject
	private RsaKey issuerNavSts;

	@BeforeAll
	public static void setUpBeforeAll() {
		TestCertificates.setupKeyAndTrustStore();
	}

	@BeforeEach
	public void setUp() {
		WireMock.reset();
		WireMock.resetAllRequests();
		WireMock.removeAllMappings();

		OIDC_TOKEN_PERSON_USER_TEST = "Bearer " + createOidc(defaultClaimsBuilder().issuer(NAV_STS_ISSUER_URL).build());
	}

	protected HttpEntity createHttpEntity() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_PERSON_USER_TEST);
		return new HttpEntity(headers);
	}

	protected HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_PERSON_USER_TEST);
		return headers;
	}


	protected void abacPermit() {
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected void abacDenyPep6d() {
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(Scenario.STARTED)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP3));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP3)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP4));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP4)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP5));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP5)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP6D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP6D)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));
	}

	protected void abacDenyPep5() {
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(Scenario.STARTED)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP3));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP3)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP4));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP4)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP5));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP5)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));
	}

	protected void abacDenyPep4() {
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(Scenario.STARTED)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP3));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP3)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP4));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP4)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));

	}

	protected void abacDenyPep3() {
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(Scenario.STARTED)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP3));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP3)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));
	}

	protected void abacDenyPep2d() {
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(Scenario.STARTED)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));
	}

	protected void abacDenyPep2() {
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(Scenario.STARTED)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));
	}

	protected void abacDenyPep1g() {
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));
	}

	protected void abacDenyPep2dDokumentOversikt() {
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(Scenario.STARTED)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json"))
				.willSetStateTo(STATE_PEP3));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP3)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP4));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP4)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP5));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP5)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP6D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP6D)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected void abacDenyPep5DokumentOversikt() {
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(Scenario.STARTED)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP3));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP3)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP4));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP4)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP5));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP5)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json"))
				.willSetStateTo(STATE_PEP6D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP6D)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected void abacDenyPep2dAndPep6dDokumentOversikt() {
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(Scenario.STARTED)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json"))
				.willSetStateTo(STATE_PEP3));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP3)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP4));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP4)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP5));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP5)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json"))
				.willSetStateTo(STATE_PEP6D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP6D)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));
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
			verify(7, postRequestedFor(urlEqualTo("/abac")));
		} else {
			verify(3, postRequestedFor(urlEqualTo("/abac")));
		}
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep3AndHttpStatusCode(HttpStatus expectedHttpStatus, HttpStatus actualHttpStatus) {
		verify(4, postRequestedFor(urlEqualTo("/abac")));
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep4AndHttpStatusCode(HttpStatus expectedHttpStatus, HttpStatus actualHttpStatus) {
		verify(5, postRequestedFor(urlEqualTo("/abac")));
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep5AndHttpStatusCode(boolean isDokumentoversikt, HttpStatus expectedHttpStatus, HttpStatus actualHttpStatus) {
		if (isDokumentoversikt) {
			verify(7, postRequestedFor(urlEqualTo("/abac")));
		} else {
			verify(6, postRequestedFor(urlEqualTo("/abac")));
		}
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep6dAndHttpStatusCode(HttpStatus expectedHttpStatus, HttpStatus actualHttpStatus) {
		verify(7, postRequestedFor(urlEqualTo("/abac")));
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyEmptyJournalpostListeAndNullSideInfo(Dokumentoversikt dokumentoversikt) {
		assertEquals(0, dokumentoversikt.getJournalposter().size());
		assertNull(dokumentoversikt.getSideInfo());
	}

	protected void assertSaksbehanlerHarTilgang(Dokumentoversikt dokumentoversikt) {
		dokumentoversikt.getJournalposter().forEach(
				journalpost -> journalpost.getDokumenter().forEach(
						dokumentInfo -> dokumentInfo.getDokumentvarianter().forEach(
								dokumentvariant -> assertTrue(dokumentvariant.isSaksbehandlerHarTilgang())))
		);
	}

	protected void assertSaksbehanlerHarIkkeTilgang(Dokumentoversikt dokumentoversikt) {
		dokumentoversikt.getJournalposter().forEach(
				journalpost -> journalpost.getDokumenter().forEach(
						dokumentInfo -> dokumentInfo.getDokumentvarianter().forEach(
								dokumentvariant -> assertFalse(dokumentvariant.isSaksbehandlerHarTilgang())))
		);
	}

	protected String stringFromClasspath(String resourcename) throws IOException {
		return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(resourcename));
	}

	public String createOidc(JwtClaims claims) {
		try {
			RsaJsonWebKey rsaJsonWebKey = issuerNavSts.getWebKey();
			JsonWebSignature jws = new JsonWebSignature();
			jws.setPayload(claims.toJson());
			jws.setKey(rsaJsonWebKey.getPrivateKey());
			jws.setKeyIdHeaderValue(rsaJsonWebKey.getKeyId());
			jws.setAlgorithmHeaderValue("RS256");
			return jws.getCompactSerialization();
		} catch (JoseException e) {
			throw new OidcAuthorizationException("Failed to convert JwtClaims to Oidc token", e);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static JwtClaimsBuilder defaultClaimsBuilder() {
		return new JwtClaimsBuilder()
				.subject("sub")
				.audience("aud")
				.expiry(LocalDateTime.now().plusMinutes(10))
				.validFrom(LocalDateTime.now().minusMinutes(5))
				.azp("azp");
	}
}
