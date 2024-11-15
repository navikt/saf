package no.nav.saf.anticorruptionlayer.nav;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.config.SafProperties;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.codec.DecodingException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClientRequest;

import java.util.regex.Pattern;

import static java.time.Duration.ofSeconds;
import static no.nav.saf.anticorruptionlayer.nav.NavHrOrganisasjonResponse.ja;
import static no.nav.saf.anticorruptionlayer.nav.NavHrOrganisasjonResponse.nei;
import static no.nav.saf.cache.LokalCacheConfig.HR_NAV_ORGANISASJON_CACHE;

@Slf4j
@Component
public class NavHrOrganisasjonConsumer {

	private static final Pattern ORGNUMMER_PATTERN = Pattern.compile("^\\d{9}$");
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

	@Cacheable(HR_NAV_ORGANISASJON_CACHE)
	public NavHrOrganisasjonResponse getNavOrganisasjon(String organisasjonsnummer) {
		if (organisasjonsnummer == null || !ORGNUMMER_PATTERN.matcher(organisasjonsnummer).matches()) {
			log.error("organisasjonsnummer={} er ikke et orgnummer (9 siffer). Kan ikke slå opp i HR NAV Orgnummer tjenesten. Må undersøkes", organisasjonsnummer);
			return nei(organisasjonsnummer);
		}

		return webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/ords/dvh/dt_hr/nav_organisasjon_orgnummer")
						.queryParam("q", "{json}")
						.build("{\"nav_org_nr\":\"" + organisasjonsnummer + "\"}"))
				.httpRequest(httpRequest -> {
					HttpClientRequest reactorRequest = httpRequest.getNativeRequest();
					reactorRequest.responseTimeout(ofSeconds(5));
				})
				.retrieve()
				.bodyToMono(NavHrOrganisasjonORDSResponse.class)
				.transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
				.switchIfEmpty(Mono.error(new DecodingException("Tom respons fra endepunkt")))
				.doOnError(Throwable.class, e -> logError(organisasjonsnummer, circuitBreaker, e))
				.mapNotNull(response -> response.count() > 0 ? ja(organisasjonsnummer) : nei(organisasjonsnummer))
				.onErrorReturn(nei(organisasjonsnummer))
				.block();
	}

	private void logError(String organisasjonsnummer, CircuitBreaker circuitBreaker, Throwable e) {
		if (e instanceof DecodingException) {
			log.error("Klarte ikke dekode payload fra HR NAV Orgnummer tjenesten. Returnerer at organisasjonsnummer={} ikke er NAV organisasjon. message={}",
					organisasjonsnummer, e.getMessage(), e);
		} else if (e instanceof CallNotPermittedException) {
			log.error("Circuitbreaker til HR NAV Orgnummer tjenesten har state={}. Returnerer at organisasjonsnummer={} ikke er NAV organisasjon. message={}",
					circuitBreaker.getState(), organisasjonsnummer, e.getMessage(), e);
		} else if (e instanceof WebClientException) {
			log.error("Kall til HR NAV Orgnummer tjenesten feilet. Returnerer at organisasjonsnummer={} ikke er NAV organisasjon. message={}",
					organisasjonsnummer, e.getMessage(), e);
		} else {
			log.error("Kall til HR NAV Orgnummer tjenesten feilet med en ukjent teknisk feil. message={}", e.getMessage(), e);
		}
	}
}
