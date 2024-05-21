package no.nav.saf.anticorruptionlayer.joark.safintern;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.CallIdExchangeFilterFunction;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivJournalpost;
import no.nav.saf.config.SafProperties;
import no.nav.saf.exceptions.JournalpostIkkeFunnetException;
import no.nav.saf.exceptions.SafTechnicalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.codec.CodecProperties;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.function.Consumer;

import static no.nav.saf.azure.AzureProperties.CLIENT_REGISTRATION_DOKARKIV;
import static no.nav.saf.headers.NavHeaders.NAV_CALLID;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Slf4j
@Service
public class DokarkivTilknyttetDokumentConsumer {
	private static final String DOKARKIV_METADATA = "dokarkivmetadata";

	private final WebClient webClient;
	private final CircuitBreaker dokarkivMetadataCircuitBreaker;
	private final Retry dokarkivMetadataRetry;

	@Autowired
	public DokarkivTilknyttetDokumentConsumer(final SafProperties safProperties,
											  final CodecProperties codecProperties,
											  final WebClient webClient,
											  final CircuitBreakerRegistry circuitBreakerRegistry,
											  final RetryRegistry retryRegistry) {
		this.webClient = webClient.mutate()
				.baseUrl(safProperties.getEndpoints().getDokarkiv().getUrl())
				.filter(new CallIdExchangeFilterFunction(NAV_CALLID))
				.exchangeStrategies(ExchangeStrategies.builder()
						.codecs(clientCodecConfigurer ->
								clientCodecConfigurer.defaultCodecs()
										.maxInMemorySize((int) codecProperties.getMaxInMemorySize().toBytes())
						)
						.build())
				.build();
		this.dokarkivMetadataCircuitBreaker = circuitBreakerRegistry.circuitBreaker(DOKARKIV_METADATA);
		this.dokarkivMetadataRetry = retryRegistry.retry(DOKARKIV_METADATA);
	}

	public List<ArkivJournalpost> hentTilknyttetJournalpost(String dokumentInfoId) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/tilknyttedeJournalposter/gjenbruk/dokumentInfoId/{dokumentInfoId}")
						.build(dokumentInfoId))
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKARKIV))
				.accept(APPLICATION_JSON)
				.retrieve()
				.bodyToMono(new ParameterizedTypeReference<List<ArkivJournalpost>>() {
				})
				.doOnError(handleErrorTilknyttetDokumentinfo(dokumentInfoId))
				.transformDeferred(CircuitBreakerOperator.of(dokarkivMetadataCircuitBreaker))
				.transformDeferred(RetryOperator.of(dokarkivMetadataRetry))
				.block();
	}

	private Consumer<Throwable> handleErrorTilknyttetDokumentinfo(String dokumentinfoId) {
		return error -> {
			if (error instanceof WebClientResponseException.NotFound notFound) {
				throw new JournalpostIkkeFunnetException("Fant ingen journalposter tilknyttet dokumentinfoId=" + dokumentinfoId);
			}
			throw new SafTechnicalException("Henting av journalposter tilknytttet dokumentinfoId=" + dokumentinfoId + " feilet med ukjent teknisk feil.", error);
		};
	}
}