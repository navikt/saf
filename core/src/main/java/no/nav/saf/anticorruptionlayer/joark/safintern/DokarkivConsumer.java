package no.nav.saf.anticorruptionlayer.joark.safintern;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.CallIdExchangeFilterFunction;
import no.nav.saf.anticorruptionlayer.joark.safintern.hentdokument.HentDokumentResponseTo;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivJournalpost;
import no.nav.saf.config.SafProperties;
import no.nav.saf.exceptions.DokumentIkkeFunnetException;
import no.nav.saf.exceptions.JournalpostIkkeFunnetException;
import no.nav.saf.exceptions.NginxException;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.codec.CodecProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Set;
import java.util.function.Consumer;

import static java.lang.String.format;
import static no.nav.saf.azure.AzureProperties.CLIENT_REGISTRATION_DOKARKIV;
import static no.nav.saf.headers.NavHeaders.NAV_CALLID;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Slf4j
@Service
public class DokarkivConsumer {
	private static final String DOKARKIV_HENTDOKUMENT = "dokarkivhentdokument";
	private static final String DOKARKIV_METADATA = "dokarkivmetadata";

	private final WebClient webClient;
	private final CircuitBreaker dokarkivHentdokumentCircuitBreaker;
	private final CircuitBreaker dokarkivMetadataCircuitBreaker;
	private final Retry dokarkivHentdokumentRetry;
	private final Retry dokarkivMetadataRetry;

	@Autowired
	public DokarkivConsumer(final SafProperties safProperties,
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
		this.dokarkivHentdokumentCircuitBreaker = circuitBreakerRegistry.circuitBreaker(DOKARKIV_HENTDOKUMENT);
		this.dokarkivMetadataCircuitBreaker = circuitBreakerRegistry.circuitBreaker(DOKARKIV_METADATA);
		this.dokarkivHentdokumentRetry = retryRegistry.retry(DOKARKIV_HENTDOKUMENT);
		this.dokarkivMetadataRetry = retryRegistry.retry(DOKARKIV_METADATA);
	}

	public HentDokumentResponseTo hentDokument(String dokumentInfoId, String variantFormat) {
		return webClient.get()
				.uri(uriBuilder -> uriBuilder.path("/hentdokument/{dokumentInfoId}/{variantFormat}")
						.build(dokumentInfoId, variantFormat))
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKARKIV))
				.accept(APPLICATION_PDF)
				.exchangeToMono(clientResponse -> {
					if (clientResponse.statusCode().is2xxSuccessful()) {
						return clientResponse.bodyToMono(byte[].class)
								.map(responseBytes -> new HentDokumentResponseTo(responseBytes,
										clientResponse.headers().asHttpHeaders().getContentType()));
					} else {
						return clientResponse.createError();
					}
				})
				.doOnError(handleErrorHentDokument(dokumentInfoId, variantFormat))
				.transformDeferred(CircuitBreakerOperator.of(dokarkivHentdokumentCircuitBreaker))
				.transformDeferred(RetryOperator.of(dokarkivHentdokumentRetry))
				.block();
	}

	private Consumer<Throwable> handleErrorHentDokument(String dokumentInfoId, String variantFormat) {
		return error -> {
			if (error instanceof WebClientResponseException.NotFound notFound) {
				handleMidlertidigNginxError(notFound);
				throw new DokumentIkkeFunnetException(format("Dokument med dokumentInfoId=%s, variantFormat=%s ikke funnet. feilmelding=%s",
						dokumentInfoId, variantFormat, error.getMessage()));
			}
			if (error instanceof WebClientResponseException webException) {
				if (webException.getStatusCode().is4xxClientError()) {
					throw new SafFunctionalException(format("Henting av dokument fra fagarkivet feilet funksjonelt. dokumentInfoId=%s, variantFormat=%s, status=%s. feilmelding=%s",
							dokumentInfoId, variantFormat, webException.getStatusCode(), error.getMessage()), error, webException.getStatusCode());
				} else {
					throw new SafTechnicalException(format("Henting av dokument fra fagarkivet feilet teknisk. dokumentInfoId=%s, variantFormat=%s, status=%s. feilmelding=%s",
							dokumentInfoId, variantFormat, webException.getStatusCode(), error.getMessage()), error, webException.getStatusCode());
				}
			}
		};
	}

	public ArkivJournalpost journalpostById(String journalpostId, Set<String> fields) {
		return webClient.get()
				.uri(uriBuilder -> {
					uriBuilder.pathSegment("journalpost", "journalpostId", "{journalpostId}");
					if (!fields.isEmpty()) {
						uriBuilder.queryParam("fields", String.join(",", fields));
					}
					return uriBuilder.build(journalpostId);
				})
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKARKIV))
				.accept(APPLICATION_JSON)
				.retrieve()
				.bodyToMono(ArkivJournalpost.class)
				.doOnError(handleErrorJournalpostById(journalpostId))
				.transformDeferred(CircuitBreakerOperator.of(dokarkivMetadataCircuitBreaker))
				.transformDeferred(RetryOperator.of(dokarkivMetadataRetry))
				.block();
	}

	private Consumer<Throwable> handleErrorJournalpostById(String journalpostId) {
		return error -> {
			if (error instanceof WebClientResponseException.NotFound notFound) {
				handleMidlertidigNginxError(notFound);
				throw new JournalpostIkkeFunnetException("Journalpost med journalpostId=" + journalpostId + " ikke funnet.");
			}
			throw new SafTechnicalException("Henting av journalpostId=" + journalpostId + " feilet med ukjent teknisk feil.", error);
		};
	}

	public ArkivJournalpost journalpostByEksternReferanseId(String eksternReferanseId, Set<String> fields) {
		return webClient.get()
				.uri(uriBuilder -> {
					uriBuilder.pathSegment("journalpost", "eksternReferanseId", "{eksternReferanseId}");
					if (!fields.isEmpty()) {
						uriBuilder.queryParam("fields", String.join(",", fields));
					}
					return uriBuilder.build(eksternReferanseId);
				})
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKARKIV))
				.accept(APPLICATION_JSON)
				.retrieve()
				.bodyToMono(ArkivJournalpost.class)
				.doOnError(handleErrorJournalpostByEksternReferanseId(eksternReferanseId))
				.transformDeferred(CircuitBreakerOperator.of(dokarkivMetadataCircuitBreaker))
				.transformDeferred(RetryOperator.of(dokarkivMetadataRetry))
				.block();
	}

	private Consumer<Throwable> handleErrorJournalpostByEksternReferanseId(String eksternReferanseId) {
		return error -> {
			if (error instanceof WebClientResponseException.NotFound notFound) {
				handleMidlertidigNginxError(notFound);
				throw new JournalpostIkkeFunnetException("Journalpost med eksternReferanseId=" + eksternReferanseId + " ikke funnet.");
			}
			throw new SafTechnicalException("Henting av eksternReferanseId=" + eksternReferanseId + " feilet med ukjent teknisk feil.", error);
		};
	}

	public ArkivJournalpost journalpostByIdAndDokumentInfoId(String journalpostId, String dokumentInfoId, Set<String> fields) {
		return webClient.get()
				.uri(uriBuilder -> {
					uriBuilder.pathSegment("journalpost", "journalpostId", "{journalpostId}", "dokumentInfoId", "{dokumentInfoId}");
					if (!fields.isEmpty()) {
						uriBuilder.queryParam("fields", String.join(",", fields));
					}
					return uriBuilder.build(journalpostId, dokumentInfoId);
				})
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKARKIV))
				.accept(APPLICATION_JSON)
				.retrieve()
				.bodyToMono(ArkivJournalpost.class)
				.doOnError(handleErrorJournalpostByIdAndDokumentInfoId(journalpostId, dokumentInfoId))
				.transformDeferred(CircuitBreakerOperator.of(dokarkivMetadataCircuitBreaker))
				.transformDeferred(RetryOperator.of(dokarkivMetadataRetry))
				.block();
	}

	private Consumer<Throwable> handleErrorJournalpostByIdAndDokumentInfoId(String journalpostId, String dokumentInfoId) {
		return error -> {
			if (error instanceof WebClientResponseException.NotFound notFound) {
				handleMidlertidigNginxError(notFound);
				throw new DokumentIkkeFunnetException(format("Journalpost med journalpostId=%s, dokumentInfoId=%s ikke funnet i Joark.",
						journalpostId, dokumentInfoId));
			}
			if (error instanceof WebClientResponseException webException) {
				if (webException.getStatusCode().is4xxClientError()) {
					throw new SafFunctionalException(format("hentJournalpost feilet funksjonelt. status=%s, journalpostId=%s, dokumentInfoId=%s. Feilmelding=%s",
							webException.getStatusCode(), journalpostId, dokumentInfoId, webException.getMessage()));
				} else {
					throw new SafTechnicalException(format("hentJournalpost feilet teknisk. status=%s, journalpostId=%s, dokumentInfoId=%s. Feilmelding=%s",
							webException.getStatusCode(), journalpostId, dokumentInfoId, webException.getMessage()), webException, webException.getStatusCode());
				}
			}
		};
	}

	private static void handleMidlertidigNginxError(WebClientResponseException.NotFound notFound) {
		String responseBody = notFound.getResponseBodyAs(String.class);
		if (isNginxResponse(notFound, responseBody)) {
			throw new NginxException("Midlertidig feil mot nginx loadbalancer. Forsøker retry", notFound);
		}
	}

	private static boolean isNginxResponse(WebClientResponseException.NotFound notFound, String responseBody) {
		return responseBody != null && responseBody.contains("nginx") && TEXT_HTML.equals(notFound.getHeaders().getContentType());
	}
}