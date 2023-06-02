package no.nav.saf;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.saf.config.AzureProperties;
import no.nav.saf.config.SafProperties;
import no.nav.saf.config.ServiceuserAlias;
import no.nav.saf.config.WebProxyProperties;
import no.nav.saf.metrics.DokMonitoringAspect;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.io.SocketConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.hc.core5.util.Timeout.ofSeconds;

@EnableAspectJAutoProxy
@ComponentScan
@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties(value = {SafProperties.class, ServiceuserAlias.class, AzureProperties.class, WebProxyProperties.class})
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

	@Bean
	public ClientHttpRequestFactory azureTokenHttpRequestFactory(WebProxyProperties webProxyProperties) {
		var readTimeout = SocketConfig.custom().setSoTimeout(ofSeconds(20)).build();
		var connectionManager = new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(40);
		connectionManager.setDefaultMaxPerRoute(10);
		connectionManager.setDefaultSocketConfig(readTimeout);


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
