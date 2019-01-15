package no.nav.saf.tilgangskontroll.validation.config;

import lombok.Data;
import no.nav.saf.tilgangskontroll.validation.registry.Idp;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;

@Data
@Validated
@ConfigurationProperties("security.oidc")
public class IdpProperties {
	private final Map<String, Idp> idp = new HashMap<>();
}