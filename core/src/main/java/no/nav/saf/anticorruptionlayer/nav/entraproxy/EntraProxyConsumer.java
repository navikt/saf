package no.nav.saf.anticorruptionlayer.nav.entraproxy;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.config.SafProperties;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Set;

import static no.nav.saf.integration.token.NaisTexasAndCallIdRequestInterceptor.TARGET_SCOPE;
import static org.springframework.boot.http.client.ClientHttpRequestFactorySettings.defaults;

@Slf4j
@Component
public class EntraProxyConsumer {

	private static final String ENTRA_PROXY_INSTANCE = "entra-proxy";
	private static final String TEMA_ANSATT_PATH = "/api/v1/tema/ansatt/{navIdent}";

	private final RestClient texasAuthorizedRestClient;
	private final SafProperties safProperties;

	public EntraProxyConsumer(RestClient texasAuthorizedRestClient,
							  SafProperties safProperties) {
		ClientHttpRequestFactorySettings settings = defaults()
				.withConnectTimeout(Duration.ofSeconds(3))
				.withReadTimeout(Duration.ofSeconds(4));
		this.texasAuthorizedRestClient = texasAuthorizedRestClient.mutate()
				.requestFactory(ClientHttpRequestFactoryBuilder.jdk().build(settings))
				.baseUrl(safProperties.getEndpoints().getEntraProxy().getUrl())
				.build();
		this.safProperties = safProperties;
	}

	@Retry(name = ENTRA_PROXY_INSTANCE)
	@CircuitBreaker(name = ENTRA_PROXY_INSTANCE)
	public EntraProxyTematilgangResponse hentTematilgangForNavAnsatt(SafRequestContext safRequestContext) {
		try {
			if (safRequestContext.isUserIdNavAnsatt()) {
				var response = texasAuthorizedRestClient.get()
						.uri(TEMA_ANSATT_PATH, safRequestContext.getUserId())
						.attribute(TARGET_SCOPE, safProperties.getEndpoints().getEntraProxy().getScope())
						.retrieve()
						.body(new ParameterizedTypeReference<Set<String>>() {});

				return new EntraProxyTematilgangResponse(response != null ? response : Set.of());

			} else {
				log.error("Kunne ikke gjøre kall mot entra-proxy fordi userId ikke er en NAV-ident");
				throw new SafFunctionalException("Kunne ikke gjøre kall mot entra-proxy fordi brukers ident ikke er en NAV-ident");
			}
		} catch (Exception e) {
			log.error("Kall mot entra-proxy feilet med en ukjent teknisk feil. message={}", e.getMessage(), e);
			throw e;
		}
	}

}
