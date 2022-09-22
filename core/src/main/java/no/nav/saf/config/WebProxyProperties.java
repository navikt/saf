package no.nav.saf.config;

import org.apache.http.HttpHost;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Optional;

@ConfigurationProperties("https.proxy")
public record WebProxyProperties(String proxy) {

	public Optional<HttpHost> getProxy() {
		return Optional.ofNullable(proxy)
				.map(string -> string.split(":"))
				.map(spec -> new HttpHost(spec[0], Integer.parseInt(spec[1])));
	}
}
