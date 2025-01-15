package no.nav.saf.anticorruptionlayer.k9.hentrelevanteparter;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import no.nav.saf.anticorruptionlayer.CallIdExchangeFilterFunction;
import no.nav.saf.config.SafProperties;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.function.Consumer;

import static no.nav.saf.azure.AzureProperties.CLIENT_REGISTRATION_K9_SAK;
import static no.nav.saf.headers.NavHeaders.NAV_CALLID;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;


@Component
public class K9Consumer {
	private static final String K9_INSTANCE = "k9sak";

	private final WebClient webClient;
	private final CircuitBreaker circuitBreaker;

	public K9Consumer(SafProperties safProperties,
					  WebClient webClient,
					  CircuitBreakerRegistry circuitBreakerRegistry) {
		this.webClient = webClient.mutate()
				.baseUrl(safProperties.getEndpoints().getK9sak().getUrl())
				.filter(new CallIdExchangeFilterFunction(NAV_CALLID))
				.defaultHeaders(headers -> headers.setContentType(APPLICATION_JSON))
				.build();
		this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(K9_INSTANCE);
	}

	public List<String> hentAktoerForSak(final String sakId) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder
						.queryParam("saksnummer", sakId)
						.build())
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_K9_SAK))
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<String>>() {})
				.doOnError(handleError(sakId))
				.transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
				.block();
	}

	private Consumer<Throwable> handleError(final String sakId) {
		return error -> {
			if (error instanceof WebClientResponseException exception) {
				var feilmelding = "Klarte ikke hente k9sak for sakId=%s, feilmelding=%s".formatted(sakId, exception.getResponseBodyAsString());
				var statusCode = exception.getStatusCode();

				if (statusCode.is4xxClientError() && !statusCode.isSameCodeAs(UNAUTHORIZED)) {
					throw new SafFunctionalException(feilmelding, statusCode);
				}

				throw new SafTechnicalException(feilmelding, statusCode);
			}
		};
	}
}
