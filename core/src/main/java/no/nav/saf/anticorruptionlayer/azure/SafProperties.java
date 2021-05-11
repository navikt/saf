package no.nav.saf.anticorruptionlayer.azure;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Data
@ConfigurationProperties("saf")
@Validated
public class SafProperties {

	private final Endpoints endpoints = new Endpoints();

	@Data
	@Validated
	public static class Endpoints {
		/**
		 * URL til PDL (Persondataløsningen).
		 */
		@NotEmpty
		private String pdl;
	}

}
