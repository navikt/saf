package no.nav.saf;

import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class},
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = {"spring.main.allow-bean-definition-overriding=true"})
@EnableMockOAuth2Server
@ActiveProfiles("itest")
class ApplicationIT {
	@Inject
	private TestRestTemplate testRestTemplate;

	@Test
	@DisplayName("Tester at applikasjonen starter opp")
	void shouldStartApp() {
		String isAlive = testRestTemplate.getForObject("/isAlive", String.class);
		assertEquals("Application is alive!", isAlive);
	}
}