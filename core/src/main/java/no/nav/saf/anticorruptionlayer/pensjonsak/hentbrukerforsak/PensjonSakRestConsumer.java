package no.nav.saf.anticorruptionlayer.pensjonsak.hentbrukerforsak;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.pensjonsak.domain.SakSammendrag;
import no.nav.saf.config.SafProperties;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static java.lang.String.format;
import static no.nav.saf.azure.AzureProperties.CLIENT_REGISTRATION_PENSJON;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Slf4j
@Component
public class PensjonSakRestConsumer {
	private static final String PENSJON_SAK_REST_INSTANCE = "pensjonsakrest";

	private final WebClient webClient;
	private final Retry retry;
	private final CircuitBreaker circuitBreaker;

	public PensjonSakRestConsumer(SafProperties safProperties,
								  WebClient webClient,
								  CircuitBreakerRegistry circuitBreakerRegistry,
								  RetryRegistry retryRegistry
	) {
		this.webClient = webClient.mutate()
				.baseUrl(safProperties.getEndpoints().getPensjon().getUrl())
				.build();
		this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(PENSJON_SAK_REST_INSTANCE);
		this.retry = retryRegistry.retry(PENSJON_SAK_REST_INSTANCE);
	}

	public HentBrukerForSakResponseTo hentBrukerForSak(final String sakId) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/pip/hentBrukerOgEnhetstilgangerForSak/v1")
						.build())
				.headers(httpHeaders -> httpHeaders.set("sakId", sakId))
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_PENSJON))
				.retrieve()
				.bodyToMono(HentBrukerForSakResponseTo.class)
				.transformDeferred(RetryOperator.of(retry))
				.transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
				.doOnError(handleHentBrukerForSakErrors())
				.block();
	}

	private Consumer<? super Throwable> handleHentBrukerForSakErrors() {
		return error -> {
			if (error instanceof WebClientResponseException webClientResponseException) {
				if (webClientResponseException.getStatusCode().is4xxClientError()) {
					throw new SafFunctionalException(format("hentBrukerForSak feilet funksjonelt med status=%s. Feilmelding=%s",
							webClientResponseException.getStatusCode(), webClientResponseException.getMessage()), webClientResponseException, webClientResponseException.getStatusCode());
				} else {
					throw new SafTechnicalException(format("hentBrukerForSak feilet teknisk med status=%s. Feilmelding=%s",
							webClientResponseException.getStatusCode(), webClientResponseException.getMessage()), webClientResponseException, webClientResponseException.getStatusCode());
				}
			}
		};
	}

	public List<SakSammendrag> hentSakSammendragListe(String personident) {
		if (isBlank(personident)) {
			return Collections.emptyList();
		}

		return webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/sak/sammendrag")
						.build())
				.headers(httpHeaders -> httpHeaders.set("fnr", personident))
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_PENSJON))
				.retrieve()
				.bodyToFlux(SakSammendrag.class)
				.transformDeferred(RetryOperator.of(retry))
				.transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
				.doOnError(handleHentSakSammendragErrors())
				.collectList()
				.block();
	}

	private Consumer<? super Throwable> handleHentSakSammendragErrors() {
		return error -> {
			if (error instanceof WebClientResponseException.NotFound notFound) {
				throw new PersonHarIngenPensjonssakerException(format("hentSakSammendrag feilet funksjonelt (pensjon har ingen saker på denne personen). status=%s. Feilmelding=%s",
						notFound.getStatusCode(), notFound.getMessage()), notFound.getStatusCode());
			}
			if (error instanceof WebClientResponseException webClientResponseException) {
				if (webClientResponseException.getStatusCode().is4xxClientError()) {
					throw new SafFunctionalException(format("hentSakSammendrag feilet funksjonelt med statusKode=%s. Feilmelding=%s",
							webClientResponseException.getStatusCode(), webClientResponseException.getMessage()), webClientResponseException, webClientResponseException.getStatusCode());
				} else {
					throw new SafTechnicalException(format("hentSakSammendrag feilet teknisk med statusKode=%s. Feilmelding=%s",
							webClientResponseException.getStatusCode(), webClientResponseException.getMessage()), webClientResponseException, webClientResponseException.getStatusCode());
				}
			}
		};
	}

}
