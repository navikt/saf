package no.nav.saf.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Data
@ConfigurationProperties("saf")
@Validated
public class SafProperties {

	private final Endpoints endpoints = new Endpoints();
	private final Redis redis = new Redis();

	@NotEmpty
	private String privilegiedserviceusers;

	@NotEmpty
	private String azureGroupEgenAnsattObjectId;

	@Data
	@Validated
	public static class Endpoints {
		private String overrideMsGraphServiceRoot;

		/**
		 * Baseurl til NAV HR tjenestene
		 */
		@NotEmpty
		private String hrNavUrl;

		/**
		 * Base URL og scope til pensjon
		 */
		@NotNull
		private AzureEndpoint pensjon;

		/**
		 * URL og scope til PDL
		 */
		@NotNull
		private AzureEndpoint pdl;

		/**
		 * URL og scope til safintern oppslagstjenesten i dokarkiv.
		 */
		@NotNull
		private AzureEndpoint dokarkiv;

		/**
		 * Url og scope til fpsak PIP
		 */
		@NotNull
		private AzureEndpoint fpsak;

		/**
		 * Url og scope til k9sak
		 */
		@NotNull
		private AzureEndpoint k9sak;

	}

	@Data
	@Validated
	public static class AzureEndpoint {
		/**
		 * Url til tjeneste som har azure autorisasjon
		 */
		@NotEmpty
		private String url;
		/**
		 * Scope til azure client credential flow
		 */
		@NotEmpty
		private String scope;
	}

	@Data
	@Validated
	public static class Redis {
		@NotEmpty
		private String hostname = "saf-redis";
		private int portnumber = 6379;
	}
}
