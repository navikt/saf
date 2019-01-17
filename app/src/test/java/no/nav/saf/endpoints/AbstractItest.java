package no.nav.saf.endpoints;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.modig.testcertificates.TestCertificates;
import no.nav.saf.ApplicationConfig;
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
