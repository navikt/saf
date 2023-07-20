package no.nav.saf.config;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

@Data
@ConfigurationProperties("saf")
@Validated
public class SafProperties {

	private final Endpoints endpoints = new Endpoints();
	private final Redis redis = new Redis();

	@NotEmpty
	private String privilegiedserviceusers;

	@Data
	@Validated
	public static class Endpoints {
		/**
		 * URL til PDL (Persondataløsningen).
		 */
		@NotEmpty
		private String pdl;

		/**
		 * URI til PEN: hent sak sammendrag (PESYS REST/ Pensjon).
		 */
		@NotEmpty
		private String penSakSammendrag;

		/**
		 * URI til PEN: hent bruker for sak (PESYS REST/ Pensjon).
		 */
		@NotEmpty
		private String penBrukerForSak;

		/**
		 * Scope for Azure Oauth mot PEN (PESYS REST/ Pensjon).
		 */
		@NotEmpty
		private String penScope;

		public URI getPenSakSammendragURI() {
			return URI.create(penSakSammendrag);
		}

		public URI getPenBrukerForSakURI() {
			return URI.create(penBrukerForSak);
		}

		/**
		 * URL til safintern oppslagstjenesten i dokarkiv.
		 */
		@NotNull
		private AzureEndpoint dokarkiv;
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
