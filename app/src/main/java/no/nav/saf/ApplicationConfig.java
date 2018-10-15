package no.nav.saf;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.saf.context.config.fasit.ServiceuserAlias;
import no.nav.saf.metrics.DokTimedAspect;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@EnableAspectJAutoProxy
@EnableConfigurationProperties(ServiceuserAlias.class)
@ComponentScan
@Configuration
@EnableAutoConfiguration
public class ApplicationConfig {

	@Bean
	ClientHttpRequestFactory requestFactory(HttpClient httpClient) {
		return new HttpComponentsClientHttpRequestFactory(httpClient);
	}

	@Bean
	HttpClient httpClient() {
		return HttpClients.createDefault();
	}

	@Bean
	DokTimedAspect timedAspect(MeterRegistry meterRegistry) {
		return new DokTimedAspect(meterRegistry);
	}
}
