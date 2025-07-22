package no.nav.saf.integration.token;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.config.NaisProperties;
import no.nav.saf.config.SafProperties;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.integration.azure.TokenResponse;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Service
public class NaisTexasConsumer {
	private static final String NAIS_TEXAS_INSTANCE = "naistexas";

	private final SafProperties safProperties;
	private final ObjectMapper objectMapper;
	private final RestClient restClient;

	public NaisTexasConsumer(SafProperties safProperties,
							 NaisProperties naisProperties,
							 ObjectMapper objectMapper,
							 ClientHttpRequestFactory azureTokenHttpRequestFactory,
							 RestClient.Builder restClientBuilder) {
		this.safProperties = safProperties;
		this.objectMapper = objectMapper;
		this.restClient = restClientBuilder
				.requestFactory(azureTokenHttpRequestFactory)
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

		TokenResponse tokenResponse = restClient
				.post()
				.accept(APPLICATION_JSON)
				.contentType(APPLICATION_FORM_URLENCODED)
				.body(formData)
				.retrieve()
				.onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
					ProblemDetail problemDetail = objectMapper.readValue(response.getBody(), ProblemDetail.class);
					throw new SafFunctionalException(format("Kall mot nais texas feilet med status=%s, feilmelding=%s",
							response.getStatusCode(), problemDetail));
				})
				.onStatus(HttpStatusCode::is5xxServerError, (request, response) -> {
					ProblemDetail problemDetail = objectMapper.readValue(response.getBody(), ProblemDetail.class);
					throw new SafTechnicalException(format("Kall mot nais texas feilet teknisk med status=%s, feilmelding=%s",
							response.getStatusCode(), problemDetail));
				})
				.body(TokenResponse.class);

		return new OboToken(requireNonNull(tokenResponse).access_token());
	}
}
