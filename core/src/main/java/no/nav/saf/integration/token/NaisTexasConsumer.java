package no.nav.saf.integration.token;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.config.NaisProperties;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.integration.azure.TokenResponse;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Service
public class NaisTexasConsumer {
	private static final String NAIS_TEXAS_INSTANCE = "naistexas";
	private static final Pattern TARGET_PATTERN = Pattern.compile("api://[^.]+\\.[^.]+\\.[^.]+/\\.default");

	private final RestClient restClient;
	private final NaisProperties naisProperties;

	public NaisTexasConsumer(NaisProperties naisProperties,
							 ClientHttpRequestFactory clientHttpRequestFactory,
							 RestClient.Builder restClientBuilder) {
		this.naisProperties = naisProperties;
		this.restClient = restClientBuilder
				.requestFactory(clientHttpRequestFactory)
				.build();
	}

	/**
	 * Utveksle en Entra-ID token mot en OBO-token for å sende request til et annet system, vha Texas
	 * @param targetScope Maskin man vil autorisere mot på format api://<cluster>.<namespace>.<other-api-app-name>/.default
	 * @return Bearer token
	 */
	@Retry(name = NAIS_TEXAS_INSTANCE)
	@CircuitBreaker(name = NAIS_TEXAS_INSTANCE)
	public String exchangeForTilgangsmaskinenOboToken(String accessToken, String targetScope) {

		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("identity_provider", "azuread");
		formData.add("target", targetScope);
		formData.add("user_token", accessToken);

		try {
			TokenResponse tokenResponse = restClient
					.post()
					.uri(naisProperties.getTokenExchangeEndpoint())
					.accept(APPLICATION_JSON)
					.contentType(APPLICATION_FORM_URLENCODED)
					.body(formData)
					.retrieve()
					.body(TokenResponse.class);

			return requireNonNull(tokenResponse).access_token();
		} catch (Exception e) {
			throw new SafTechnicalException("Kall mot texas feilet", e);
		}
	}

	/**
	 * Maskin-til-maskin systemtoken fra Texas
	 * @param targetScope Maskin man vil autorisere mot på format api://<cluster>.<namespace>.<other-api-app-name>/.default
	 * @return Bearer token
	 */
	@Retry(name = NAIS_TEXAS_INSTANCE)
	@CircuitBreaker(name = NAIS_TEXAS_INSTANCE)
	public String getSystemToken(String targetScope) {
		if (isBlank(targetScope) || !TARGET_PATTERN.matcher(targetScope).matches()) {
			throw new IllegalArgumentException("Ugyldig targetScope. Må være på format api://<cluster>.<namespace>.<other-api-app-name>/.default");
		}

		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("identity_provider", "azuread");
		formData.add("target", targetScope);
		return requireNonNull(restClient.post()
				.uri(naisProperties.getTokenEndpoint())
				.contentType(APPLICATION_FORM_URLENCODED)
				.body(formData)
				.retrieve()
				.body(TokenResponse.class))
				.access_token();
	}
}
