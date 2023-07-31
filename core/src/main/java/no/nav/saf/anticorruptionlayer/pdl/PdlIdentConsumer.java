package no.nav.saf.anticorruptionlayer.pdl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import no.nav.saf.anticorruptionlayer.CallIdExchangeFilterFunction;
import no.nav.saf.config.SafProperties;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static no.nav.saf.azure.AzureProperties.CLIENT_REGISTRATION_PDL;
import static no.nav.saf.azure.AzureProperties.getOAuth2AuthorizeRequestForAzure;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

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

	private final SafProperties safProperties;
	private final WebClient webClient;
	private final ReactiveOAuth2AuthorizedClientManager oAuth2AuthorizedClientManager;

	public PdlIdentConsumer(SafProperties safProperties,
							WebClient webClient,
							ReactiveOAuth2AuthorizedClientManager oAuth2AuthorizedClientManager) {
		this.safProperties = safProperties;
		this.oAuth2AuthorizedClientManager = oAuth2AuthorizedClientManager;
		this.webClient = webClient.mutate()
				.filter(new CallIdExchangeFilterFunction(HEADER_PDL_NAV_CALL_ID))
				.defaultHeaders(headers -> {
					headers.setContentType(APPLICATION_JSON);
					headers.set(HEADER_PDL_BEHANDLINGSNUMMER, ARKIVPLEIE_BEHANDLINGSNUMMER);
				})
				.build();
	}

	@Retry(name = PDL_INSTANCE)
	@CircuitBreaker(name = PDL_INSTANCE)
	@Override
	public List<PdlResponse.PdlIdent> hentIdenter(final String ident) throws PersonIkkeFunnetException {
		PdlResponse pdlResponse = webClient.post()
				.uri(safProperties.getEndpoints().getPdl().getUrl())
				.attributes(getOAuth2AuthorizedClient())
				.bodyValue(mapHentIdenterQuery(ident))
				.retrieve()
				.bodyToMono(PdlResponse.class)
				.doOnError(handleErrorPdl())
				.block();

		if (pdlResponse.getErrors() == null || pdlResponse.getErrors().isEmpty()) {
			return pdlResponse.getData().getHentIdenter().getIdenter();
		} else {
			if (PERSON_IKKE_FUNNET_CODE.equals(pdlResponse.getErrors().get(0).getExtensions().getCode())) {
				throw new PersonIkkeFunnetException("Fant ikke person i Persondataløsningen (PDL).");
			}
			throw new PdlFunctionalException("Kunne ikke hente aktørid for folkeregisterident i pdl. " + pdlResponse.getErrors());
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

	private Consumer<Throwable> handleErrorPdl() {
		return error -> {
			if (error instanceof WebClientResponseException response && response.getStatusCode().is4xxClientError()) {
				throw new PdlFunctionalException("Kall mot pdl feilet funksjonelt.", error);
			}
		};
	}

	private Consumer<Map<String, Object>> getOAuth2AuthorizedClient() {
		Mono<OAuth2AuthorizedClient> clientMono = oAuth2AuthorizedClientManager.authorize(getOAuth2AuthorizeRequestForAzure(CLIENT_REGISTRATION_PDL));
		return oauth2AuthorizedClient(clientMono.block());
	}
}