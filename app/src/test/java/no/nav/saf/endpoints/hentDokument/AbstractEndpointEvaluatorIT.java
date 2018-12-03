package no.nav.saf.endpoints.hentDokument;

import static no.nav.saf.endpoints.JwtClaimsBuilderProvider.openAmClaimsBuilder;

import com.auth0.jwt.JWT;
import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.freg.security.test.oidc.tools.OidcTestService;
import no.nav.freg.security.test.oidc.tools.TestToolsAutoConfig;
import no.nav.saf.ApplicationConfig;
import no.nav.saf.endpoints.testconfig.TestConfig;
import org.apache.cxf.helpers.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import java.io.IOException;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ApplicationConfig.class, TestToolsAutoConfig.class, TestConfig.class},
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

}
