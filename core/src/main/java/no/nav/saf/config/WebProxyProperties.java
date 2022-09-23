package no.nav.saf.config;

import org.apache.http.HttpHost;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Optional;

@ConfigurationProperties("https")
public record WebProxyProperties(String proxyHost, int proxyPort) {

	public Optional<HttpHost> getProxy() {
		return Optional.ofNullable(proxyHost)
				.map(spec -> new HttpHost(proxyHost, proxyPort));
	}
}
