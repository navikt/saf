package no.nav.saf.anticorruptionlayer.gsak.hentgsaksaker;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.config.ServiceuserAlias;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.metrics.Monitor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static java.time.Duration.ofSeconds;
import static no.nav.saf.util.MDCConstants.CALL_ID;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;

@Slf4j
@Component
public class GsakConsumer {
	private static final String SAK_INSTANCE = "sak";

	private static final String HEADER_CORRELATION_ID = "X-Correlation-ID";

	private final RestTemplate restTemplate;
	private final String gsakApiUrl;

	public GsakConsumer(RestTemplateBuilder restTemplateBuilder,
						ClientHttpRequestFactory clientHttpRequestFactory,
						@Value("${sak.saker.url}") String gsakApiUrl,
						ServiceuserAlias serviceuserAlias) {
		this.gsakApiUrl = gsakApiUrl;
		this.restTemplate = restTemplateBuilder
				.requestFactory(() -> clientHttpRequestFactory)
				.setReadTimeout(ofSeconds(20))
				.basicAuthentication(serviceuserAlias.getUsername(), serviceuserAlias.getPassword())
				.build();
	}

	@CircuitBreaker(name = SAK_INSTANCE)
	@Retry(name = SAK_INSTANCE)
	@Monitor(value = "dok_consumer", extraTags = {"process", "hentSakerByAktoerId"}, histogram = true)
	public List<GsakSakerTo> hentSakerByAktoerIder(final List<String> aktoerIder) {
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(gsakApiUrl)
				.queryParam("aktoerId", aktoerIder);
		return hentSaker(uri.toUriString());
	}


	@CircuitBreaker(name = SAK_INSTANCE)
	@Retry(name = SAK_INSTANCE)
	@Monitor(value = "dok_consumer", extraTags = {"process", "hentSakerByAktoerId"}, histogram = true)
	public List<GsakSakerTo> hentSakerByAktoerId(final String aktoerId) {
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(gsakApiUrl)
				.queryParam("aktoerId", aktoerId);
		return hentSaker(uri.toUriString());
	}

	@CircuitBreaker(name = SAK_INSTANCE)
	@Retry(name = SAK_INSTANCE)
	@Monitor(value = "dok_consumer", extraTags = {"process", "hentSakerByAktoerId"}, histogram = true)
	public List<GsakSakerTo> hentSakerByAktoerIder(final List<String> aktoerIder, final Tema tema) {
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(gsakApiUrl)
				.queryParam("tema", tema.toString())
				.queryParam("aktoerId", aktoerIder);
		return hentSaker(uri.toUriString());
	}

	@CircuitBreaker(name = SAK_INSTANCE)
	@Retry(name = SAK_INSTANCE)
	@Monitor(value = "dok_consumer", extraTags = {"process", "hentSakerByOrgNr"}, histogram = true)
	public List<GsakSakerTo> hentSakerByOrgNr(final String orgNr) {
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(gsakApiUrl)
				.queryParam("orgnr", orgNr);
		return hentSaker(uri.toUriString());
	}

	@CircuitBreaker(name = SAK_INSTANCE)
	@Retry(name = SAK_INSTANCE)
	@Monitor(value = "dok_consumer", extraTags = {"process", "hentSakerByOrgNr"}, histogram = true)
	public List<GsakSakerTo> hentSakerByOrgNr(final String orgNr, final Tema tema) {
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(gsakApiUrl)
				.queryParam("orgnr", orgNr)
				.queryParam("tema", tema.toString());
		return hentSaker(uri.toUriString());
	}

	@CircuitBreaker(name = SAK_INSTANCE)
	@Retry(name = SAK_INSTANCE)
	@Monitor(value = "dok_consumer", extraTags = {"process", "hentSakerByFagsakIdAndFagsaksystem"}, histogram = true)
	public List<GsakSakerTo> hentSakerByFagsakIdAndFagsaksystem(final String fagsakId, final String fagsaksystem) {
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(gsakApiUrl)
				.queryParam("fagsakNr", fagsakId)
				.queryParam("applikasjon", fagsaksystem);
		return hentSaker(uri.toUriString());
	}

	private List<GsakSakerTo> hentSaker(final String uri) {
		if (log.isDebugEnabled()) {
			log.debug("Henter gsaker uri={}", uri);
		}
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set(HEADER_CORRELATION_ID, getOrGenerateCorrelationId());
			ResponseEntity<List<GsakSakerTo>> response = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<List<GsakSakerTo>>() {
			});
			if (log.isDebugEnabled()) {
				log.debug("Hentet ferdig gsaker uri={}", uri);
			}
			return response.getBody();
		} catch (HttpServerErrorException e) {
			throw new SafTechnicalException(String.format("getGsaksaker feilet teknisk med statusKode=%s. Feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e, e.getStatusCode());
		} catch (HttpClientErrorException e) {
			throw new SafFunctionalException(String.format("getGsaksaker feilet funksjonelt med statusKode=%s. Feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e, e.getStatusCode());
		}
	}

	private String getOrGenerateCorrelationId() {
		String callId = trim(MDC.get(CALL_ID));
		if (isBlank(callId)) {
			return UUID.randomUUID().toString();
		}
		return callId;
	}
}
