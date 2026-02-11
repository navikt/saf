package no.nav.saf;

import no.nav.saf.azure.AzureProperties;
import no.nav.saf.config.NaisProperties;
import no.nav.saf.config.SafProperties;
import no.nav.saf.config.ServiceuserAlias;
import no.nav.saf.config.WebProxyProperties;
import no.nav.saf.integration.token.NaisTexasAndCallIdRequestInterceptor;
import no.nav.saf.integration.token.NaisTexasConsumer;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.io.SocketConfig;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import static org.apache.hc.core5.util.Timeout.ofSeconds;

@ComponentScan
@Configuration
@EnableAutoConfiguration(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableConfigurationProperties(value = {
		SafProperties.class,
		ServiceuserAlias.class,
		AzureProperties.class,
		NaisProperties.class,
		WebProxyProperties.class
})
public class ApplicationConfig {
	@Bean
	ClientHttpRequestFactory clientHttpRequestFactory(HttpClient httpClient) {
		HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		// Default timeouts for alle restklienter som bruker denne requestFactory.
		// RestTemplate som behøver egne timeouts må konstruere en ny ClientHttpRequestFactory.
		httpComponentsClientHttpRequestFactory.setConnectTimeout(5_000);
		return httpComponentsClientHttpRequestFactory;
	}

	@Bean
	HttpClient httpClient() {
		var readTimeout = SocketConfig.custom().setSoTimeout(ofSeconds(20)).build();
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(400);
		connectionManager.setDefaultMaxPerRoute(100);
		connectionManager.setDefaultSocketConfig(readTimeout);

		return HttpClients.custom()
				.setConnectionManager(connectionManager)
				.build();
	}

	@Bean
	RestClient texasAuthorizedRestClient(ClientHttpRequestFactory clientHttpRequestFactory, NaisTexasConsumer naisTexasConsumer) {
		return RestClient.builder()
				.requestFactory(clientHttpRequestFactory)
				.requestInterceptor(new NaisTexasAndCallIdRequestInterceptor(naisTexasConsumer))
				.build();
	}

	@Bean
	ClientHttpRequestFactory hentJournalsakInfoClientHttpRequestFactory(HttpClient hentJournalsakInfoHttpClient) {
		HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(hentJournalsakInfoHttpClient);
		httpComponentsClientHttpRequestFactory.setConnectTimeout(5_000);
		return httpComponentsClientHttpRequestFactory;
	}

	@Bean
	HttpClient hentJournalsakInfoHttpClient() {
		var readTimeout = SocketConfig.custom().setSoTimeout(ofSeconds(180)).build();
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(200);
		connectionManager.setDefaultMaxPerRoute(200);
		connectionManager.setDefaultSocketConfig(readTimeout);

		return HttpClients.custom()
				.setConnectionManager(connectionManager)
				.build();
	}
}
