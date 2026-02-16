package no.nav.saf.anticorruptionlayer.nav.entraproxy;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.config.SafProperties;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Set;

import static no.nav.saf.cache.LokalCacheConfig.ENTRA_PROXY_TEMA_CACHE;
import static no.nav.saf.integration.token.NaisTexasAndCallIdRequestInterceptor.TARGET_SCOPE;
import static org.springframework.boot.http.client.ClientHttpRequestFactorySettings.defaults;

@Slf4j
@Component
public class EntraProxyConsumer {

	private static final String ENTRA_PROXY_INSTANCE = "entra-proxy";
	private static final String TEMA_ANSATT_PATH = "/api/v1/tema/ansatt/{navIdent}";

	private final RestClient texasAuthorizedRestClient;
	private final SafProperties safProperties;

	public EntraProxyConsumer(RestClient texasAuthorizedRestClient,
							  SafProperties safProperties) {
		ClientHttpRequestFactorySettings settings = defaults()
				.withConnectTimeout(Duration.ofSeconds(3))
				.withReadTimeout(Duration.ofSeconds(4));
		this.texasAuthorizedRestClient = texasAuthorizedRestClient.mutate()
				.requestFactory(ClientHttpRequestFactoryBuilder.jdk().build(settings))
				.baseUrl(safProperties.getEndpoints().getEntraProxy().getUrl())
				.build();
		this.safProperties = safProperties;
	}

	@Cacheable(ENTRA_PROXY_TEMA_CACHE)
	@Retry(name = ENTRA_PROXY_INSTANCE)
	@CircuitBreaker(name = ENTRA_PROXY_INSTANCE)
	public EntraProxyTematilgangResponse hentTematilgangForNavAnsatt(String navIdent) {
		try {
			var response = texasAuthorizedRestClient.get()
					.uri(TEMA_ANSATT_PATH, navIdent)
					.attribute(TARGET_SCOPE, safProperties.getEndpoints().getEntraProxy().getScope())
					.retrieve()
					.body(new ParameterizedTypeReference<Set<String>>() {
					});

			return new EntraProxyTematilgangResponse(response != null ? response : Set.of());
		} catch (Exception e) {
			throw new EntraProxyException("Kunne ikke hente tema tilganger for Nav ansatt", e);
		}
	}
}
