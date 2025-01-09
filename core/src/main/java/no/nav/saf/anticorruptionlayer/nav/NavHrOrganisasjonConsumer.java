package no.nav.saf.anticorruptionlayer.nav;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.config.SafProperties;
import org.springframework.core.codec.DecodingException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientRequest;
import reactor.util.retry.Retry;

import java.time.Duration;

import static java.time.Duration.ofSeconds;

@Slf4j
@Component
public class NavHrOrganisasjonConsumer {

	private static final String NAV_HR_ORGANISASJON_INSTANCE = "navhrorganisasjon";

	private final WebClient webClient;
	private final CircuitBreaker circuitBreaker;

	public NavHrOrganisasjonConsumer(WebClient webClient,
									 SafProperties safProperties,
									 CircuitBreakerRegistry circuitBreakerRegistry) {
		this.webClient = webClient.mutate()
				.baseUrl(safProperties.getEndpoints().getHrNavUrl())
				.build();
		this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(NAV_HR_ORGANISASJON_INSTANCE);
	}

	NavHrOrganisasjonORDSResponse getAllNavOrganisasjon() {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/ords/dvh/dt_hr/nav_organisasjon_orgnummer")
						.build())
				.httpRequest(httpRequest -> {
					HttpClientRequest reactorRequest = httpRequest.getNativeRequest();
					reactorRequest.responseTimeout(ofSeconds(5));
				})
				.retrieve()
				.bodyToMono(NavHrOrganisasjonORDSResponse.class)
				.transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
				.switchIfEmpty(Mono.error(new DecodingException("Tom respons fra endepunkt")))
				.doOnError(Throwable.class, e -> logError(circuitBreaker, e))
				.retryWhen(Retry.backoff(100, Duration.ofSeconds(3)))
				.block();
	}

	private void logError(CircuitBreaker circuitBreaker, Throwable e) {
		if (e instanceof DecodingException) {
			log.error("Klarte ikke dekode payload fra HR NAV Orgnummer tjenesten. Får ikke lastet cache. message={}",
					e.getMessage(), e);
		} else if (e instanceof CallNotPermittedException) {
			log.error("Circuitbreaker til HR NAV Orgnummer tjenesten har state={}. Får ikke lastet cache. message={}",
					circuitBreaker.getState(), e.getMessage(), e);
		} else if (e instanceof WebClientException) {
			log.error("Kall til HR NAV Orgnummer tjenesten feilet. Får ikke lastet cache. message={}", e.getMessage(), e);
		} else {
			log.error("Kall til HR NAV Orgnummer tjenesten feilet med en ukjent teknisk feil. message={}", e.getMessage(), e);
		}
	}
}
