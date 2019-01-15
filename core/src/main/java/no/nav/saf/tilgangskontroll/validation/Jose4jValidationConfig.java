package no.nav.saf.tilgangskontroll.validation;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.tilgangskontroll.validation.registry.IdpRegistry;
import org.jose4j.http.Get;
import org.jose4j.http.SimpleGet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class Jose4jValidationConfig {

	@Bean
	Jose4jConsumerFactory jose4jConsumerFactory(SimpleGet simpleGet, IdpRegistry idpRegistry) {
		return new Jose4jConsumerFactory(simpleGet, idpRegistry.getAll());
	}

	@Bean
	SimpleGet simpleGet() {
		return new Get();
	}
}