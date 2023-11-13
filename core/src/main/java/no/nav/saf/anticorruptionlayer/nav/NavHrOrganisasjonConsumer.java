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
	public NavHrOrganisasjonResponse getNavBedrift(String organisasjonsnummer) {
		if (organisasjonsnummer == null || !ORGNUMMER_PATTERN.matcher(organisasjonsnummer).matches()) {
			log.error("organisasjonsnummer={} er ikke et orgnummer (9 siffer). Kan ikke slå opp i HR NAV Orgnummer tjenesten. Må undersøkes", organisasjonsnummer);
			return nei(organisasjonsnummer);
		}

		return webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/json/Hr/Nav_Orgnummer/ER_NAV_ORGNUMMER")
						.queryParam("ORGNUMMER_INN", organisasjonsnummer)
						.build())
				.httpRequest(httpRequest -> {
					HttpClientRequest reactorRequest = httpRequest.getNativeRequest();
					reactorRequest.responseTimeout(ofSeconds(5));
				})
				.exchangeToMono(clientResponse -> {
					if (clientResponse.statusCode().is2xxSuccessful()) {
						return clientResponse.bodyToMono(NavHrOrganisasjonResponse.class);
					} else {
						return clientResponse.createError();
					}
				})
				.transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
				.switchIfEmpty(Mono.error(new DecodingException("Tom respons fra endepunkt")))
				.doOnError(DecodingException.class, e ->
						log.error("Klarte ikke dekode payload fra HR NAV Orgnummer tjenesten. Returnerer at organisasjonsnummer={} ikke er NAV organisasjon. message={}",
								organisasjonsnummer, e.getMessage(), e))
				.doOnError(CallNotPermittedException.class, e ->
						log.error("Circuitbreaker til HR NAV Orgnummer tjenesten har state={}. Returnerer at organisasjonsnummer={} ikke er NAV organisasjon. message={}",
								circuitBreaker.getState(), organisasjonsnummer, e.getMessage(), e))
				.doOnError(WebClientException.class, e ->
						log.error("Kall til HR NAV Orgnummer tjenesten feilet. Returnerer at organisasjonsnummer={} ikke er NAV organisasjon. message={}",
								organisasjonsnummer, e.getMessage(), e))
				.onErrorReturn(nei(organisasjonsnummer))
				.block();
	}
}
