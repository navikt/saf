package no.nav.saf.anticorruptionlayer.fpsak.hentrelevanteparter;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import no.nav.saf.config.SafProperties;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.function.Consumer;

import static no.nav.saf.azure.AzureProperties.CLIENT_REGISTRATION_FPSAK;
import static no.nav.saf.cache.LokalCacheConfig.FPSAK_RELEVANTE_PARTER_BY_SAKID_CACHE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Component
public class FpsakConsumer {
	private static final String FPSAK_INSTANCE = "fpsak";

	private final WebClient webClient;
	private final CircuitBreaker circuitBreaker;

	public FpsakConsumer(WebClient webClient,
						 CircuitBreakerRegistry circuitBreakerRegistry,
						 SafProperties safProperties) {
		this.webClient = webClient.mutate()
				.baseUrl(safProperties.getEndpoints().getFpsak().getUrl())
				.build();
		this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(FPSAK_INSTANCE);
	}

	@Cacheable(cacheNames = FPSAK_RELEVANTE_PARTER_BY_SAKID_CACHE, key = "#sakId")
	public List<String> hentAktoerForSak(final String sakId) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder.queryParam("saksnummer", sakId).build())
				.accept(APPLICATION_JSON)
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_FPSAK))
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<String>>() {
				})
				.doOnError(handleErrors(sakId))
				.transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
				.block();
	}

	private Consumer<Throwable> handleErrors(final String sakId) {
		return error -> {
			if (error instanceof WebClientResponseException webException) {
				if (webException.getStatusCode().is4xxClientError()) {
					throw new SafFunctionalException("Klarte ikke hente fpsak for sakId=" + sakId + ", feilmelding=" + webException.getResponseBodyAsString(), webException.getStatusCode());
				} else {
					throw new SafTechnicalException("Klarte ikke hente fpsak for sakId=" + sakId + ", feilmelding=" + webException.getResponseBodyAsString(), webException.getStatusCode());
				}
			}
		};
	}
}
