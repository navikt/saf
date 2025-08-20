package no.nav.saf.anticorruptionlayer.gsak.hentgsaksaker;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.config.SafProperties;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.List;

import static java.lang.String.format;
import static no.nav.saf.integration.token.NaisTexasAndCallIdRequestInterceptor.TARGET_SCOPE;
import static no.nav.saf.util.MDCUtility.getCallId;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class GsakConsumer {
	private static final String SAK_INSTANCE = "sak";

	public static final String HEADER_CORRELATION_ID = "X-Correlation-ID";

	private final RestClient restClient;
	private final ObjectMapper objectMapper;
	private final SafProperties.AzureEndpoint gsakEndpoint;

	public GsakConsumer(RestClient restClient,
						SafProperties safProperties,
						ObjectMapper objectMapper) {
		this.gsakEndpoint = safProperties.getEndpoints().getGsak();
		this.restClient = restClient.mutate()
				.baseUrl(safProperties.getEndpoints().getGsak().getUrl())
				.defaultHeaders(httpHeaders -> {
					httpHeaders.setContentType(APPLICATION_JSON);
					httpHeaders.set(HEADER_CORRELATION_ID, getCallId());
				})
				.build();
		this.objectMapper = objectMapper;
	}

	@CircuitBreaker(name = SAK_INSTANCE)
	@Retry(name = SAK_INSTANCE)
	public List<GsakSakerTo> hentSakerByAktoerIder(final List<String> aktoerIder) {
		return restClient.get()
				.uri(uriBuilder -> uriBuilder
						.queryParam("aktoerId", aktoerIder).build())
				.attribute(TARGET_SCOPE, gsakEndpoint.getScope())
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) -> handleError(res))
				.body(new ParameterizedTypeReference<List<GsakSakerTo>>() {
				});
	}

	@CircuitBreaker(name = SAK_INSTANCE)
	@Retry(name = SAK_INSTANCE)
	public List<GsakSakerTo> hentSakerByAktoerId(final String aktoerId) {
		log.info("Henter saker for aktoerId: {}", aktoerId);
		return restClient.get()
				.uri(uriBuilder -> uriBuilder
						.queryParam("aktoerId", aktoerId)
						.build())
				.attribute(TARGET_SCOPE, gsakEndpoint.getScope())
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) -> handleError(res))
				.body(new ParameterizedTypeReference<List<GsakSakerTo>>() {
				});
	}

	@CircuitBreaker(name = SAK_INSTANCE)
	@Retry(name = SAK_INSTANCE)
	public List<GsakSakerTo> hentSakerByAktoerIder(final List<String> aktoerIder, final Tema tema) {
		return restClient.get()
				.uri(uriBuilder -> uriBuilder
						.queryParam("tema", tema.toString())
						.queryParam("aktoerId", aktoerIder)
						.build())
				.attribute(TARGET_SCOPE, gsakEndpoint.getScope())
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) -> handleError(res))
				.body(new ParameterizedTypeReference<List<GsakSakerTo>>() {
				});
	}

	@CircuitBreaker(name = SAK_INSTANCE)
	@Retry(name = SAK_INSTANCE)
	public List<GsakSakerTo> hentSakerByOrgNr(final String orgNr) {
		return restClient.get()
				.uri(uriBuilder -> uriBuilder
						.queryParam("orgnr", orgNr)
						.build())
				.attribute(TARGET_SCOPE, gsakEndpoint.getScope())
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) -> handleError(res))
				.body(new ParameterizedTypeReference<>() {
				});
	}

	@CircuitBreaker(name = SAK_INSTANCE)
	@Retry(name = SAK_INSTANCE)
	public List<GsakSakerTo> hentSakerByOrgNr(final String orgNr, final Tema tema) {
		return restClient.get()
				.uri(uriBuilder -> uriBuilder
						.queryParam("orgnr", orgNr)
						.queryParam("tema", tema)
						.build())
				.attribute(TARGET_SCOPE, gsakEndpoint.getScope())
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) -> handleError(res))
				.body(new ParameterizedTypeReference<>() {
				});
	}

	public List<GsakSakerTo> hentSakerByFagsakIdAndFagsaksystem(final String fagsakId, final String fagsaksystem) {
		return restClient.get()
				.uri(uriBuilder -> uriBuilder
						.queryParam("fagsakNr", fagsakId)
						.queryParam("applikasjon", fagsaksystem)
						.build())
				.attribute(TARGET_SCOPE, gsakEndpoint.getScope())
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) -> handleError(res))
				.body(new ParameterizedTypeReference<>() {
				});
	}

	private void handleError(ClientHttpResponse response) throws IOException {
		ProblemDetail problemDetail = objectMapper.readValue(response.getBody(), ProblemDetail.class);
		if (response.getStatusCode().is4xxClientError()) {
			throw new SafTechnicalException(format("getGsaksaker feilet teknisk med statusKode=%s. Feilmelding=%s",
					response.getStatusCode(), problemDetail.getDetail()));
		}
		throw new SafFunctionalException(format("getGsaksaker feilet funksjonelt med statusKode=%s. Feilmelding=%s",
				problemDetail.getStatus(), problemDetail.getDetail()));
	}
}
