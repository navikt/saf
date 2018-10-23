package no.nav.saf;

import graphql.schema.GraphQLSchema;
import io.leangen.graphql.GraphQLSchemaGenerator;
import io.leangen.graphql.metadata.strategy.query.AnnotatedResolverBuilder;
import io.micrometer.core.instrument.MeterRegistry;
import no.nav.saf.endpoints.sakstilknyttedejournalposter.SakstilknyttedeJournalposter;
import no.nav.saf.integration.fasit.ServiceuserAlias;
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
@ComponentScan
@Configuration
@EnableAutoConfiguration
@EnableConfigurationProperties(ServiceuserAlias.class)
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

	@Bean
	GraphQLSchema graphQLSchema(SakstilknyttedeJournalposter sakstilknyttedeJournalposter) {
		//Schema generated from query classes
		GraphQLSchemaGenerator schemaGenerator = new GraphQLSchemaGenerator()
				.withResolverBuilders(new AnnotatedResolverBuilder());

		schemaGenerator = schemaGenerator
				.withOperationsFromSingleton(sakstilknyttedeJournalposter);

		return schemaGenerator.generate();
	}
}
