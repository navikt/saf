package no.nav.saf.endpoints.testconfig;

import no.nav.saf.integration.fasit.ServiceuserAlias;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.spring.SpringBus;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Configuration
@EnableConfigurationProperties(ServiceuserAlias.class)
@Profile("itest")
public class TestConfig {

	@Bean
	RestTemplateBuilder restTemplateBuilder() {
		return new RestTemplateBuilder();
	}

	@Bean
	ClientHttpRequestFactory requestFactory(HttpClient httpClient) {
		return new HttpComponentsClientHttpRequestFactory(httpClient);
	}

	@Bean
	HttpClient httpClient() {
		return HttpClients.createDefault();
	}

	@Bean
	public Bus bus() {
		return new SpringBus();
	}

}
