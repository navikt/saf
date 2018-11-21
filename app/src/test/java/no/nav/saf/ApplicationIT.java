package no.nav.saf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import no.nav.saf.tilgangskontroll.testconfig.STSTestConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {Application.class, STSTestConfig.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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