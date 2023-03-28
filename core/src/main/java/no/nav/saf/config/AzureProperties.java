package no.nav.saf.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;

/**
 * Konfigurert av naiserator. https://doc.nais.io/security/auth/azure-ad/#runtime-variables-credentials
 */
@ConfigurationProperties("azure.app")
@Validated
public record AzureProperties(
		@NotEmpty String tokenUrl,
		@NotEmpty String clientId,
		@NotEmpty String clientSecret,
		@NotEmpty String tenantId,
		@NotEmpty String wellKnownUrl
) {
}
