package no.nav.saf;

import no.nav.saf.anticorruptionlayer.nav.NavOrgService;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class},
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = {"spring.main.allow-bean-definition-overriding=true" })
@EnableMockOAuth2Server
@ActiveProfiles("itest")
class ApplicationIT {

	@Autowired
	private TestRestTemplate testRestTemplate;
	@MockitoBean
	protected NavOrgService navOrgService;

	@Test
	@DisplayName("Tester at applikasjonen starter opp")
	void shouldStartApp() {
		var liveness = testRestTemplate.getForEntity("/actuator/health/liveness", String.class);
		assertThat(liveness.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(liveness.getBody()).contains("UP");
	}
}