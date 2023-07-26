package no.nav.saf.anticorruptionlayer.pdl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import no.nav.saf.anticorruptionlayer.sts.StsResponse;
import no.nav.saf.anticorruptionlayer.sts.StsRestConsumer;
import no.nav.saf.config.SafProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.RequestEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static no.nav.saf.util.MDCUtility.getCallId;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * PDL implementasjon av {@link IdentConsumer}
 */
@Component
class PdlIdentConsumer implements IdentConsumer {
	private static final String PDL_INSTANCE = "pdl";
	private static final String PERSON_IKKE_FUNNET_CODE = "not_found";
	private static final String HEADER_PDL_NAV_CALL_ID = "Nav-Call-Id";
	// https://pdldocs-navno.msappproxy.net/ekstern/index.html#_dokumenter_hjemmel_vha_tema
	private static final String HEADER_PDL_BEHANDLINGSNUMMER = "behandlingsnummer";
	// https://behandlingskatalog.nais.adeo.no/process/purpose/ARKIVPLEIE/756fd557-b95e-4b20-9de9-6179fb8317e6
	private static final String ARKIVPLEIE_BEHANDLINGSNUMMER = "B315";

	private final RestTemplate restTemplate;
	private final URI pdlUri;
	private final StsRestConsumer stsRestConsumer;

	public PdlIdentConsumer(final SafProperties safProperties,
							final RestTemplateBuilder restTemplateBuilder,
							final StsRestConsumer stsRestConsumer,
							final ClientHttpRequestFactory clientHttpRequestFactory) {
		this.restTemplate = restTemplateBuilder
				.requestFactory(() -> clientHttpRequestFactory)
				.build();
		this.pdlUri = UriComponentsBuilder.fromHttpUrl(safProperties.getEndpoints().getPdl()).build().toUri();
		this.stsRestConsumer = stsRestConsumer;
	}

	@Retry(name = PDL_INSTANCE)
	@CircuitBreaker(name = PDL_INSTANCE)
	@Override
	public List<PdlResponse.PdlIdent> hentIdenter(final String ident) throws PersonIkkeFunnetException {
		try {
			final RequestEntity<PdlRequest> requestEntity = baseRequest()
					.body(mapHentIdenterQuery(ident));
			final PdlResponse pdlResponse = requireNonNull(restTemplate.exchange(requestEntity, PdlResponse.class).getBody());

			if (pdlResponse.getErrors() == null || pdlResponse.getErrors().isEmpty()) {
				return pdlResponse.getData().getHentIdenter().getIdenter();
			} else {
				if (PERSON_IKKE_FUNNET_CODE.equals(pdlResponse.getErrors().get(0).getExtensions().getCode())) {
					throw new PersonIkkeFunnetException("Fant ikke person i Persondataløsningen (PDL).");
				}
				throw new PdlFunctionalException("Kunne ikke hente aktørid for folkeregisterident i pdl. " + pdlResponse.getErrors());
			}
		} catch (HttpClientErrorException e) {
			throw new PdlFunctionalException("Kall mot pdl feilet funksjonelt.", e);
		}
	}

	private PdlRequest mapHentIdenterQuery(final String ident) {
		final HashMap<String, Object> variables = new HashMap<>();
		variables.put("ident", ident);
		return PdlRequest.builder()
				.query("query hentIdenter($ident: ID!) {hentIdenter(ident: $ident, historikk: true) {identer { ident gruppe historisk } } }")
				.variables(variables)
				.build();
	}

	private RequestEntity.BodyBuilder baseRequest() {
		StsResponse restStsToken = stsRestConsumer.getStsToken();
		return RequestEntity.post(pdlUri)
				.accept(APPLICATION_JSON)
				.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.header(AUTHORIZATION, "Bearer " + restStsToken.getAccess_token())
				.header(HEADER_PDL_NAV_CALL_ID, getCallId())
				.header(HEADER_PDL_BEHANDLINGSNUMMER, ARKIVPLEIE_BEHANDLINGSNUMMER);
	}
}