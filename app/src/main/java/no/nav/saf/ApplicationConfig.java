package no.nav.saf;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.saf.graphiql.GraphiQLController;
import no.nav.saf.integration.fasit.ServiceuserAlias;
import no.nav.saf.metrics.DokMonitoringAspect;
import no.nav.saf.platform.TomcatConfig;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.retry.annotation.EnableRetry;

import javax.inject.Named;
import java.util.Set;

@EnableAspectJAutoProxy
@ComponentScan
@Configuration
@EnableAutoConfiguration
@Import({TomcatConfig.class,
		CoreConfig.class,
		GraphiQLController.class})
@EnableConfigurationProperties(ServiceuserAlias.class)
@EnableRetry
public class ApplicationConfig {
	@Bean
	ClientHttpRequestFactory requestFactory(HttpClient httpClient) {
		return new HttpComponentsClientHttpRequestFactory(httpClient);
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
	DokMonitoringAspect timedAspect(MeterRegistry meterRegistry) {
		return new DokMonitoringAspect(meterRegistry);
	}

	@Bean
	@Named("azureIssuers")
	Set<String> azureIssuers(@Value("${security.oidc.idp.azurev1.issuerUrl}") String azurev1IssuerUrl,
							 @Value("${security.oidc.idp.azurev2.issuerUrl}") String azurev2IssuerUrl) {
		return Set.of(azurev1IssuerUrl, azurev2IssuerUrl);
	}
}
