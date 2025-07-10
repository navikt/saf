package no.nav.saf.integration.azure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.config.SafProperties;
import no.nav.security.token.support.core.jwt.JwtToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static java.lang.String.format;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

@Slf4j
@Service
public class NaisTexasConsumer {
	private static final String AZURE_OBO_TOKEN_INSTANCE = "azureobotoken";

	private static final String JWT_BEARER = "urn:ietf:params:oauth:grant-type:jwt-bearer";
	private static final String ON_BEHALF_OF = "on_behalf_of";

	private final SafProperties safProperties;
	private final ObjectMapper objectMapper;
	private final WebClient webClient;

	public NaisTexasConsumer(SafProperties safProperties,
							 @Value("${nais_token_exchange_endpoint}") String exchangeEndpoint,
							 ObjectMapper objectMapper,
							 WebClient webClient) {
		this.safProperties = safProperties;
		this.objectMapper = objectMapper;
		this.webClient = webClient.mutate()
				.baseUrl(exchangeEndpoint)
				.defaultHeader(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
				.build();
	}

	@Retry(name = AZURE_OBO_TOKEN_INSTANCE)
	@CircuitBreaker(name = AZURE_OBO_TOKEN_INSTANCE)
	public OboToken exchangeForTilgangsmaskinenOboToken(JwtToken accessToken) {

		MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
		formData.add("identity_provider", "azuread");
		formData.add("target", safProperties.getEndpoints().getTilgangsmaskinen().getScope());
		formData.add("user_token", accessToken.getEncodedToken());

		String responseJson = webClient
				.post()
				.body(BodyInserters.fromFormData(formData))
				.retrieve()
				.bodyToMono(String.class)
				.doOnError(this::handleError)
				.block();

		try {
			return new OboToken(objectMapper.readValue(responseJson, TokenResponse.class).access_token());
		} catch (JsonProcessingException e) {
			throw new NaisTexasException("Klarte ikke parse data fra Nais Texas. Feilmelding=" + e.getMessage(), e);
		}
	}

	private void handleError(Throwable error) {
		if (error instanceof WebClientResponseException response && response.getStatusCode().is4xxClientError()) {
			String feilmelding = format("Klarte ikke hente token fra Azure. Kall mot Nais Texas feilet funksjonelt med status=%s, feilmelding=%s, response=%s",
					response.getStatusCode(),
					response.getMessage(),
					response.getResponseBodyAsString());

			throw new NaisTexasException(feilmelding, error);
		} else {
			throw new NaisTexasException(format("Kall mot Nais Texas feilet teknisk med feilmelding=%s", error.getMessage()), error);
		}
	}

}
