package no.nav.saf.anticorruptionlayer.pdl;

import no.nav.saf.anticorruptionlayer.azure.TokenResponse;
import static no.nav.saf.anticorruptionlayer.pdl.NavHeaders.NAV_CALLID;
import static no.nav.saf.anticorruptionlayer.pdl.MDCUtils.getCallId;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import no.nav.saf.anticorruptionlayer.azure.AzureTokenConsumer;
import no.nav.saf.anticorruptionlayer.azure.SafProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.RequestEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * PDL implementasjon av {@link IdentConsumer}
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
class PdlIdentConsumer implements IdentConsumer {
	private static final String PDL_INSTANCE = "pdl";
	private static final String HEADER_PDL_NAV_CONSUMER_TOKEN = "Nav-Consumer-Token";
	private static final String PERSON_IKKE_FUNNET_CODE = "not_found";

	private final RestTemplate restTemplate;
	private final URI pdlUri;
	private final AzureTokenConsumer azureTokenConsumer;

	public PdlIdentConsumer(final SafProperties safProperties,
							final RestTemplateBuilder restTemplateBuilder,
							final AzureTokenConsumer azureTokenConsumer,
							final ClientHttpRequestFactory clientHttpRequestFactory) {
		this.restTemplate = restTemplateBuilder
				.setConnectTimeout(Duration.ofSeconds(3))
				.setReadTimeout(Duration.ofSeconds(20))
				.requestFactory(() -> clientHttpRequestFactory)
				.build();
		this.pdlUri = UriComponentsBuilder.fromHttpUrl(safProperties.getEndpoints().getPdl()).build().toUri();
		this.azureTokenConsumer = azureTokenConsumer;
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
					throw new PersonIkkeFunnetException("Fant ikke aktørid for person i pdl.");
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
		TokenResponse clientCredentialToken = azureTokenConsumer.getClientCredentialToken();
		return RequestEntity.post(pdlUri)
				.accept(APPLICATION_JSON)
				.header(NAV_CALLID, getCallId())
				.header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.header(AUTHORIZATION, "Bearer " + clientCredentialToken.getAccess_token());
				//.header(HEADER_PDL_NAV_CONSUMER_TOKEN, "Bearer " + clientCredentialToken.getAccess_token());
	}
}