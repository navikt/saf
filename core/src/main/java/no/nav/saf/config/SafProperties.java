package no.nav.saf.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
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
		private String pensaksammendrag;

		/**
		 * URI til PEN: hent bruker for sak (PESYS REST/ Pensjon).
		 */
		@NotEmpty
		private String penbrukerforsak;

		/**
		 * Scope for Azure Oauth mot PEN (PESYS REST/ Pensjon).
		 */
		@NotEmpty
		private String penscope;

		public URI getPenSakSammendragURI() {
			return URI.create(pensaksammendrag);
		}

		public URI getPenBrukerForSakURI() {
			return URI.create(penbrukerforsak);
		}
	}

	@Data
	@Validated
	public static class Redis {
		@NotEmpty
		private String hostname = "saf-redis";
		private int portnumber = 6379;
	}
}
