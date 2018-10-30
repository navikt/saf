package no.nav.saf.tilgangskontroll.pep;

import static no.nav.saf.tilgangskontroll.JwtClaimsBuilderProvider.openAmClaimsBuilder;

import no.nav.freg.security.oidc.idp.config.IdpConfig;
import no.nav.freg.security.test.oidc.tools.OidcTestService;
import no.nav.freg.security.test.oidc.tools.TestToolsAutoConfig;
import no.nav.saf.CoreAclConfig;
import no.nav.saf.tilgangskontroll.testconfig.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		classes = {CoreAclConfig.class, TestToolsAutoConfig.class, ServletWebServerFactoryAutoConfiguration.class, IdpConfig.class, TestConfig.class})
@ActiveProfiles("itest,wiremock,oidc")
@ImportAutoConfiguration
@AutoConfigureWireMock(port = 0)
public abstract class AbstractPepEvaluatorIT {

	protected String OIDC_TOKEN_PERSON_USER_TEST;
	protected String OIDC_TOKEN_SERVICE_USER_TEST;
	protected final String SERVICE_USER_ID = "srvsaf";
	protected final String PERSON_USER_ID = "Z990782";

	@Inject
	protected OidcTestService oidcTestService;

	@BeforeEach
	public void setUp() {
		OIDC_TOKEN_PERSON_USER_TEST = "Bearer " + oidcTestService.createOidc(openAmClaimsBuilder().subject(PERSON_USER_ID)
				.build());
		OIDC_TOKEN_SERVICE_USER_TEST = "Bearer " + oidcTestService.createOidc(openAmClaimsBuilder().subject(SERVICE_USER_ID)
				.build());
	}

}
