package no.nav.saf;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.saf.config.ServiceuserAlias;
import no.nav.saf.graphiql.GraphiQLController;
import no.nav.saf.metrics.DokMonitoringAspect;
import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

import javax.inject.Named;
import java.util.HashSet;
import java.util.Set;

@EnableAspectJAutoProxy
@ComponentScan
@Configuration
@EnableAutoConfiguration
@Import({CoreConfig.class,
		GraphiQLController.class})
@EnableConfigurationProperties(ServiceuserAlias.class)
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
	Set<String> azureIssuers(MultiIssuerConfiguration multiIssuerConfiguration) {
		Set<String> issuers = new HashSet<>();
		multiIssuerConfiguration.getIssuer("azurev1").ifPresent(issuerConfiguration -> issuers.add(issuerConfiguration.getMetaData().getIssuer().getValue()));
		multiIssuerConfiguration.getIssuer("azurev2").ifPresent(issuerConfiguration -> issuers.add(issuerConfiguration.getMetaData().getIssuer().getValue()));
		return issuers;
	}
}
