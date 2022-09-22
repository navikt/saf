package no.nav.saf;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.saf.config.AzureProperties;
import no.nav.saf.config.SafProperties;
import no.nav.saf.config.ServiceuserAlias;
import no.nav.saf.config.WebProxyProperties;
import no.nav.saf.graphiql.GraphiQLController;
import no.nav.saf.metrics.DokMonitoringAspect;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@EnableAspectJAutoProxy
@ComponentScan
@Configuration
@EnableAutoConfiguration
@Import({GraphiQLController.class})
@EnableConfigurationProperties(value = {SafProperties.class, ServiceuserAlias.class, AzureProperties.class, WebProxyProperties.class})
public class ApplicationConfig {
	@Bean
	ClientHttpRequestFactory clientHttpRequestFactory(HttpClient httpClient) {
		HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		// Default timeouts for alle restklienter som bruker denne requestFactory.
		// RestTemplate som behøver egne timeouts må konstruere en ny ClientHttpRequestFactory.
		httpComponentsClientHttpRequestFactory.setConnectTimeout(5_000);
		httpComponentsClientHttpRequestFactory.setReadTimeout(20_000);
		return httpComponentsClientHttpRequestFactory;
	}

	@Bean
	HttpClient httpClient() {
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(400);
		connectionManager.setDefaultMaxPerRoute(100);
		return HttpClients.custom()
				.setConnectionManager(connectionManager)
				.build();
	}

	@Bean
	ClientHttpRequestFactory hentJournalsakInfoClientHttpRequestFactory(HttpClient hentJournalsakInfoHttpClient) {
		HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(hentJournalsakInfoHttpClient);
		httpComponentsClientHttpRequestFactory.setConnectTimeout(5_000);
		httpComponentsClientHttpRequestFactory.setReadTimeout(180_000);
		return httpComponentsClientHttpRequestFactory;
	}

	@Bean
	HttpClient hentJournalsakInfoHttpClient() {
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(200);
		connectionManager.setDefaultMaxPerRoute(200);
		return HttpClients.custom()
				.setConnectionManager(connectionManager)
				.build();
	}

	@Bean
	public ClientHttpRequestFactory azureTokenHttpClient(WebProxyProperties webProxyProperties) {
		var connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(40);
		connectionManager.setDefaultMaxPerRoute(10);

		var httpClient = webProxyProperties.getProxy()
				.map(proxy -> HttpClients.custom()
						.setConnectionManager(connectionManager)
						.setProxy(proxy)
						.build())
				.orElseGet(() -> HttpClients.custom()
						.setConnectionManager(connectionManager)
						.build());

		var clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		clientHttpRequestFactory.setConnectTimeout(5_000);
		clientHttpRequestFactory.setReadTimeout(20_000);

		return clientHttpRequestFactory;
	}

	@Bean
	DokMonitoringAspect timedAspect(MeterRegistry meterRegistry) {
		return new DokMonitoringAspect(meterRegistry);
	}

	@Bean
	@Qualifier("privilegiedServiceusers")
	public Map<String, Boolean> getServiceusersMap(SafProperties safProperties) {
		Map<String, Boolean> privilegiedServiceusers = new HashMap<>();
		List<String> serviceuserList = Arrays.stream(StringUtils.split(safProperties.getPrivilegiedserviceusers(), ',')).collect(Collectors.toList());
		serviceuserList.forEach(serviceuser -> privilegiedServiceusers.put(serviceuser, true));
		return privilegiedServiceusers;
	}
}
