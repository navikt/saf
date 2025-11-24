package no.nav.saf.anticorruptionlayer.nav.tilgangsmaskinen;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.config.SafProperties;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.PepAnswer;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static no.nav.saf.integration.token.NaisTexasAndCallIdRequestInterceptor.TARGET_SCOPE;
import static org.springframework.boot.http.client.ClientHttpRequestFactorySettings.defaults;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.MULTI_STATUS;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@Slf4j
@Component
public class TilgangsmaskinenConsumer {

	private static final String TILGANGSMASKINEN_INSTANCE = "tilgangsmaskinen";
	private static final String BULK_CCF_KJERNEREGEL_PATH = "/api/v1/bulk/ccf/{ansattId}/KJERNE_REGELTYPE";
	private static final String CCF_KOMPLETT_PATH = "/api/v1/ccf/komplett/{ansattId}";

	private final RestClient texasAuthorizedRestClient;
	private final SafProperties safProperties;

	public static final Duration READ_TIMEOUT = Duration.ofSeconds(2);

	public TilgangsmaskinenConsumer(RestClient texasAuthorizedRestClient, SafProperties safProperties) {
		ClientHttpRequestFactorySettings settings = defaults()
				.withConnectTimeout(Duration.ofSeconds(3))
				.withReadTimeout(READ_TIMEOUT);
		this.texasAuthorizedRestClient = texasAuthorizedRestClient.mutate()
				.requestFactory(ClientHttpRequestFactoryBuilder.jdk().build(settings))
				.baseUrl(safProperties.getEndpoints().getTilgangsmaskinen().getUrl())
				.build();
		this.safProperties = safProperties;
	}

	@Retry(name = TILGANGSMASKINEN_INSTANCE)
	@CircuitBreaker(name = TILGANGSMASKINEN_INSTANCE)
	public PepAnswer navIdentHasAccess(String identifikator, SafRequestContext safRequestContext, String pepName) {
		return navIdentHasAccess(identifikator, safRequestContext, CCF_KOMPLETT_PATH, pepName);
	}

	@Retry(name = TILGANGSMASKINEN_INSTANCE)
	@CircuitBreaker(name = TILGANGSMASKINEN_INSTANCE)
	public PepAnswer navIdentHasAccessBulk(List<String> identifikatorliste, SafRequestContext safRequestContext, String pepName) {
		return navIdentHasAccess(identifikatorliste, safRequestContext, BULK_CCF_KJERNEREGEL_PATH, pepName);
	}

	private PepAnswer navIdentHasAccess(Object body, SafRequestContext safRequestContext, String path, String pepName) {
		try {
			if (safRequestContext.isUserIdNavAnsatt()) {
				return texasAuthorizedRestClient.post()
						.uri(uriBuilder -> uriBuilder.path(path).build(safRequestContext.getUserId()))
						.attributes(attributes ->
								attributes.put(TARGET_SCOPE, safProperties.getEndpoints().getTilgangsmaskinen().getScope()))
						.body(body)
						.exchange((request, response) -> handleResponseFromTilgangsmaskinen(response, pepName));
			} else {
				log.error("Kunne ikke gjøre kall mot tilgangsmaskinen fordi userId ikke er en NAV-ident");
				throw new SafFunctionalException("Kunne ikke gjøre kall mot tilgangsmaskinen fordi brukers ident ikke er en NAV-ident");
			}
		} catch (Exception e) {
			log.error("Kall mot Tilgangsmaskinen feilet med en ukjent teknisk feil. message={}", e.getMessage(), e);
			throw e;
		}
	}

	private PepAnswer handleResponseFromTilgangsmaskinen(RestClient.RequestHeadersSpec.ConvertibleClientHttpResponse response, String pepName) throws IOException {
		try (response) {
			if (FORBIDDEN.equals(response.getStatusCode())) {
				return TilgangsmaskinenResponseMapper.map(response.bodyTo(TilgangsmaskinenDenyAnswer.class), pepName);
			} else if (NO_CONTENT.equals(response.getStatusCode())) {
				return PepAnswer.permit();
			} else if (MULTI_STATUS.equals(response.getStatusCode())) { //"OK" for bulk-kall
				return TilgangsmaskinenResponseMapper.map(response.bodyTo(TilgangsmaskinenBulkResponse.class), pepName);
			} else if (response.getStatusCode().is4xxClientError() && !NOT_FOUND.equals(response.getStatusCode())) {
				throw new SafFunctionalException("Kall mot tilgangsmaskinen feilet med status " +
												 response.getStatusCode() + " " + response.getStatusText(), response.getStatusCode());
			} else {
				throw new SafTechnicalException("kall mot tilgangsmaskinen fikk uventet status " +
												response.getStatusCode() + " " + response.getStatusText(), response.getStatusCode());
			}
		}
	}
}
