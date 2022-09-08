package no.nav.saf.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Data
@ConfigurationProperties("saf")
@Validated
public class SafProperties {

	private final Endpoints endpoints = new Endpoints();
	private final Redis redis = new Redis();
	private final Proxy proxy = new Proxy();

	public Optional<Proxy> getProxy() {
		if (proxy.isSet())
			return Optional.of(proxy);
		return Optional.empty();
	}

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
	}

	@Data
	@Validated
	public static class Redis {
		@NotEmpty
		private String hostname = "saf-redis";
		private int portnumber = 6379;
	}

	@Data
	@Validated
	public static class Proxy {
		private String host;
		private int port;

		public boolean isSet() {
			return isNotBlank(host);
		}
	}
}
