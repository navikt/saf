package no.nav.saf.tilgangskontroll.validation.config;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.tilgangskontroll.validation.registry.IdpRegistry;
import no.nav.saf.tilgangskontroll.validation.registry.IdpRegistryImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(IdpProperties.class)
public class IdpConfig {

	@Bean
	@ConditionalOnMissingBean
	IdpRegistry idpRegistry(IdpProperties properties) {
		properties.getIdp().forEach((k, v) -> log.info("Registering Identity provider: {}", k));

		return new IdpRegistryImpl(properties.getIdp());
	}
}