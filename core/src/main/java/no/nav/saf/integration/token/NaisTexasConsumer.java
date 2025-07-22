package no.nav.saf.integration.token;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.config.NaisProperties;
import no.nav.saf.config.SafProperties;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.integration.azure.TokenResponse;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import static java.util.Objects.requireNonNull;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Service
public class NaisTexasConsumer {
	private static final String NAIS_TEXAS_INSTANCE = "naistexas";

	private final SafProperties safProperties;
	private final RestClient restClient;

	public NaisTexasConsumer(SafProperties safProperties,
							 NaisProperties naisProperties,
							 ClientHttpRequestFactory clientHttpRequestFactory,
							 RestClient.Builder restClientBuilder) {
		this.safProperties = safProperties;
		this.restClient = restClientBuilder
				.requestFactory(clientHttpRequestFactory)
				.baseUrl(naisProperties.getTokenExchangeEndpoint())
				.build();
	}

	@Retry(name = NAIS_TEXAS_INSTANCE)
	@CircuitBreaker(name = NAIS_TEXAS_INSTANCE)
	public OboToken exchangeForTilgangsmaskinenOboToken(JwtToken accessToken) {

		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("identity_provider", "azuread");
		formData.add("target", safProperties.getEndpoints().getTilgangsmaskinen().getScope());
		formData.add("user_token", accessToken.getEncodedToken());

		try {
			TokenResponse tokenResponse = restClient
					.post()
					.accept(APPLICATION_JSON)
					.contentType(APPLICATION_FORM_URLENCODED)
					.body(formData)
					.retrieve()
					.body(TokenResponse.class);

			return new OboToken(requireNonNull(tokenResponse).access_token());
		} catch (Exception e) {
			throw new SafTechnicalException("Kall mot texas feilet", e);
		}
	}
}
