package no.nav.saf.anticorruptionlayer.joark.safintern;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.CallIdExchangeFilterFunction;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
import no.nav.saf.anticorruptionlayer.joark.safintern.hentdokument.HentDokumentResponseTo;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivJournalpost;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.JournalpostJournalstatusRequest;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.PaginatedArkivJournalpost;
import no.nav.saf.config.SafProperties;
import no.nav.saf.domain.kode.Journalposttype;
import no.nav.saf.exceptions.DokumentIkkeFunnetException;
import no.nav.saf.exceptions.JournalpostIkkeFunnetException;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import org.springframework.boot.http.codec.autoconfigure.HttpCodecsProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static no.nav.saf.azure.AzureProperties.CLIENT_REGISTRATION_DOKARKIV;
import static no.nav.saf.headers.NavHeaders.NAV_CALLID;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Slf4j
@Service
public class DokarkivConsumer {

	private static final String DOKARKIV_HENTDOKUMENT = "dokarkivhentdokument";
	static final String DOKARKIV_METADATA = "dokarkivmetadata";

	private final WebClient webClient;
	private final CircuitBreaker dokarkivHentdokumentCircuitBreaker;
	private final CircuitBreaker dokarkivMetadataCircuitBreaker;
	private final Retry dokarkivHentdokumentRetry;
	private final Retry dokarkivMetadataRetry;

	public DokarkivConsumer(final SafProperties safProperties,
							final HttpCodecsProperties httpCodecsProperties,
							final WebClient webClient,
							final CircuitBreakerRegistry circuitBreakerRegistry,
							final RetryRegistry retryRegistry) {
		this.webClient = webClient.mutate()
				.baseUrl(safProperties.getEndpoints().getDokarkiv().getUrl())
				.filter(new CallIdExchangeFilterFunction(NAV_CALLID))
				.exchangeStrategies(ExchangeStrategies.builder()
						.codecs(clientCodecConfigurer ->
								clientCodecConfigurer.defaultCodecs()
										.maxInMemorySize((int) httpCodecsProperties.getMaxInMemorySize().toBytes())
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
				.onErrorMap(error -> mapErrorHentDokument(error, dokumentInfoId, variantFormat))
				.transformDeferred(CircuitBreakerOperator.of(dokarkivHentdokumentCircuitBreaker))
				.transformDeferred(RetryOperator.of(dokarkivHentdokumentRetry))
				.block();
	}

	private Throwable mapErrorHentDokument(Throwable error, String dokumentInfoId, String variantFormat) {
		if (error instanceof WebClientResponseException.NotFound) {
			throw new DokumentIkkeFunnetException(format("Dokument med dokumentInfoId=%s, variantFormat=%s ikke funnet. feilmelding=%s",
					dokumentInfoId, variantFormat, error.getMessage()));
		}
		if (error instanceof WebClientResponseException webException) {
			if (webException.getStatusCode().is4xxClientError()) {
				throw new SafFunctionalException(format("Henting av dokument fra dokarkiv feilet funksjonelt. dokumentInfoId=%s, variantFormat=%s, status=%s. feilmelding=%s",
						dokumentInfoId, variantFormat, webException.getStatusCode(), error.getMessage()), error, webException.getStatusCode());
			}
		}
		throw new SafTechnicalException(format("Henting av dokument fra dokarkiv feilet teknisk. dokumentInfoId=%s, variantFormat=%s", dokumentInfoId, variantFormat), error);
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
				.onErrorMap(error -> mapErrorJournalpostById(error, journalpostId))
				.transformDeferred(CircuitBreakerOperator.of(dokarkivMetadataCircuitBreaker))
				.transformDeferred(RetryOperator.of(dokarkivMetadataRetry))
				.block();
	}

	private Throwable mapErrorJournalpostById(Throwable error, String journalpostId) {
		if (error instanceof WebClientResponseException.NotFound) {
			throw new JournalpostIkkeFunnetException("Journalpost med journalpostId=" + journalpostId + " ikke funnet.");
		}
		throw new SafTechnicalException("Henting av journalpostId=" + journalpostId + " feilet med ukjent teknisk feil.", error);
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
				.onErrorMap(error -> mapErrorJournalpostByEksternReferanseId(error, eksternReferanseId))
				.transformDeferred(CircuitBreakerOperator.of(dokarkivMetadataCircuitBreaker))
				.transformDeferred(RetryOperator.of(dokarkivMetadataRetry))
				.block();
	}

	private Throwable mapErrorJournalpostByEksternReferanseId(Throwable error, String eksternReferanseId) {
		if (error instanceof WebClientResponseException.NotFound) {
			throw new JournalpostIkkeFunnetException("Journalpost med eksternReferanseId=" + eksternReferanseId + " ikke funnet.");
		}
		throw new SafTechnicalException("Henting av eksternReferanseId=" + eksternReferanseId + " feilet med ukjent teknisk feil.", error);
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
				.onErrorMap(error -> mapErrorJournalpostByIdAndDokumentInfoId(error, journalpostId, dokumentInfoId))
				.transformDeferred(CircuitBreakerOperator.of(dokarkivMetadataCircuitBreaker))
				.transformDeferred(RetryOperator.of(dokarkivMetadataRetry))
				.block();
	}

	private Throwable mapErrorJournalpostByIdAndDokumentInfoId(Throwable error, String journalpostId, String dokumentInfoId) {
		if (error instanceof WebClientResponseException.NotFound) {
			throw new DokumentIkkeFunnetException(format("Journalpost med journalpostId=%s, dokumentInfoId=%s ikke funnet i Joark.",
					journalpostId, dokumentInfoId));
		}
		if (error instanceof WebClientResponseException webException) {
			if (webException.getStatusCode().is4xxClientError()) {
				throw new SafFunctionalException(format("hentJournalpost feilet funksjonelt. status=%s, journalpostId=%s, dokumentInfoId=%s. Feilmelding=%s",
						webException.getStatusCode(), journalpostId, dokumentInfoId, webException.getMessage()));
			}
		}
		throw new SafTechnicalException(format("hentJournalpost feilet teknisk. journalpostId=%s, dokumentInfoId=%s", journalpostId, dokumentInfoId), error);
	}

	public PaginatedArkivJournalpost finnJournalposterStatus(JournalStatusCode journalstatus, List<Journalposttype> journalposttype, LocalDate startDato, int antallRader, String etterPeker, Set<String> fields) {
		return webClient.post()
				.uri(uriBuilder -> {
					uriBuilder.pathSegment("finnjournalposterstatus");
					if (!fields.isEmpty()) {
						uriBuilder.queryParam("fields", String.join(",", fields));
					}
					return uriBuilder.build();
				})
				.contentType(APPLICATION_JSON)
				.bodyValue(new JournalpostJournalstatusRequest(journalstatus.name(), DateTimeFormatter.ISO_DATE.format(startDato), journalposttype.stream().map(Journalposttype::name).toList(), antallRader, etterPeker))
				.attributes(clientRegistrationId(CLIENT_REGISTRATION_DOKARKIV))
				.accept(APPLICATION_JSON)
				.retrieve()
				.bodyToMono(PaginatedArkivJournalpost.class)
				.onErrorMap(error -> handleErrorFinnJournalposterStatus(error, journalstatus, journalposttype))
				.transformDeferred(CircuitBreakerOperator.of(dokarkivMetadataCircuitBreaker))
				.transformDeferred(RetryOperator.of(dokarkivMetadataRetry))
				.block();
	}

	private Throwable handleErrorFinnJournalposterStatus(Throwable error, JournalStatusCode journalstatus, List<Journalposttype> journalposttyper) {
		if (error instanceof WebClientResponseException webException) {
			if (webException.getStatusCode().is4xxClientError()) {
				throw new SafFunctionalException(format("finnJournalposterStatus feilet funksjonelt. status=%s, journalstatus=%s, journalposttyper=%s. Feilmelding=%s",
						webException.getStatusCode(), journalstatus, journalposttyper, webException.getMessage()));
			}
		}
		throw new SafTechnicalException(format("finnJournalposterStatus feilet teknisk. journalstatus=%s, journalposttyper=%s", journalstatus, journalposttyper), error);
	}
}