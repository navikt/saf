package no.nav.saf.anticorruptionlayer.sak.hentsaksaker;

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
import static no.nav.saf.headers.NavHeaders.X_CORRELATION_ID;
import static no.nav.saf.integration.token.NaisTexasAndCallIdRequestInterceptor.TARGET_SCOPE;
import static no.nav.saf.util.MDCUtility.getCallId;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Component
public class SakConsumer {
	private static final String SAK_INSTANCE = "sak";

	private final RestClient texasRestClient;
	private final ObjectMapper objectMapper;
	private final String sakScope;

	public SakConsumer(RestClient texasRestClient,
					   SafProperties safProperties,
					   ObjectMapper objectMapper) {
		this.sakScope = safProperties.getEndpoints().getSak().getScope();
		this.texasRestClient = texasRestClient.mutate()
				.baseUrl(safProperties.getEndpoints().getSak().getUrl())
				.defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.build();
		this.objectMapper = objectMapper;
	}

	@CircuitBreaker(name = SAK_INSTANCE)
	@Retry(name = SAK_INSTANCE)
	public List<SakSakerTo> hentSakerByAktoerIder(final List<String> aktoerIder) {
		return texasRestClient.get()
				.uri(uriBuilder -> uriBuilder
						.queryParam("aktoerId", aktoerIder).build())
				.header(X_CORRELATION_ID, getCallId())
				.attribute(TARGET_SCOPE, sakScope)
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) -> handleError(res, "hentSakerByAktoerIder"))
				.body(new ParameterizedTypeReference<>() {
				});
	}

	@CircuitBreaker(name = SAK_INSTANCE)
	@Retry(name = SAK_INSTANCE)
	public List<SakSakerTo> hentSakerByAktoerId(final String aktoerId) {
		log.info("Henter saker for aktoerId: {}", aktoerId);
		return texasRestClient.get()
				.uri(uriBuilder -> uriBuilder
						.queryParam("aktoerId", aktoerId)
						.build())
				.header(X_CORRELATION_ID, getCallId())
				.attribute(TARGET_SCOPE, sakScope)
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) -> handleError(res, "hentSakerByAktoerId"))
				.body(new ParameterizedTypeReference<>() {
				});
	}

	@CircuitBreaker(name = SAK_INSTANCE)
	@Retry(name = SAK_INSTANCE)
	public List<SakSakerTo> hentSakerByAktoerIder(final List<String> aktoerIder, final Tema tema) {
		return texasRestClient.get()
				.uri(uriBuilder -> uriBuilder
						.queryParam("tema", tema.toString())
						.queryParam("aktoerId", aktoerIder)
						.build())
				.header(X_CORRELATION_ID, getCallId())
				.attribute(TARGET_SCOPE, sakScope)
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) -> handleError(res, "hentSakerByAktoerIder"))
				.body(new ParameterizedTypeReference<>() {
				});
	}

	@CircuitBreaker(name = SAK_INSTANCE)
	@Retry(name = SAK_INSTANCE)
	public List<SakSakerTo> hentSakerByOrgNr(final String orgNr) {
		return texasRestClient.get()
				.uri(uriBuilder -> uriBuilder
						.queryParam("orgnr", orgNr)
						.build())
				.header(X_CORRELATION_ID, getCallId())
				.attribute(TARGET_SCOPE, sakScope)
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) -> handleError(res, "hentSakerByOrgNr"))
				.body(new ParameterizedTypeReference<>() {
				});
	}

	@CircuitBreaker(name = SAK_INSTANCE)
	@Retry(name = SAK_INSTANCE)
	public List<SakSakerTo> hentSakerByOrgNr(final String orgNr, final Tema tema) {
		return texasRestClient.get()
				.uri(uriBuilder -> uriBuilder
						.queryParam("orgnr", orgNr)
						.queryParam("tema", tema)
						.build())
				.header(X_CORRELATION_ID, getCallId())
				.attribute(TARGET_SCOPE, sakScope)
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) -> handleError(res, "hentSakerByOrgNr"))
				.body(new ParameterizedTypeReference<>() {
				});
	}

	@CircuitBreaker(name = SAK_INSTANCE)
	@Retry(name = SAK_INSTANCE)
	public List<SakSakerTo> hentSakerByFagsakIdAndFagsaksystem(final String fagsakId, final String fagsaksystem) {
		return texasRestClient.get()
				.uri(uriBuilder -> uriBuilder
						.queryParam("fagsakNr", fagsakId)
						.queryParam("applikasjon", fagsaksystem)
						.build())
				.header(X_CORRELATION_ID, getCallId())
				.attribute(TARGET_SCOPE, sakScope)
				.retrieve()
				.onStatus(HttpStatusCode::isError, (req, res) -> handleError(res, "hentSakerByFagsakIdAndFagsaksystem"))
				.body(new ParameterizedTypeReference<>() {
				});
	}

	private void handleError(ClientHttpResponse response, String tjeneste) throws IOException {
		ProblemDetail problemDetail = objectMapper.readValue(response.getBody(), ProblemDetail.class);
		if (response.getStatusCode().is4xxClientError()) {
			throw new SafFunctionalException(format("%s feilet funksjonelt med statusKode=%s. Feilmelding=%s",
					tjeneste, response.getStatusCode(), problemDetail.getDetail()));
		}
		throw new SafTechnicalException(format("%s feilet teknisk med statusKode=%s. Feilmelding=%s",
				tjeneste, problemDetail.getStatus(), problemDetail.getDetail()));
	}
}
