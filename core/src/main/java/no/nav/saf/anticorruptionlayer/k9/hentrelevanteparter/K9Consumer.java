package no.nav.saf.anticorruptionlayer.k9.hentrelevanteparter;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import no.nav.saf.anticorruptionlayer.sts.StsRestConsumer;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static no.nav.saf.headers.NavHeaders.NAV_CALLID;
import static no.nav.saf.util.MDCUtility.getCallId;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class K9Consumer {
	private static final String K9_INSTANCE = "k9sak";

	private final String k9Url;
	private final RestTemplate restTemplate;
	private final StsRestConsumer stsRestConsumer;

	public K9Consumer(RestTemplateBuilder restTemplateBuilder,
					  ClientHttpRequestFactory clientHttpRequestFactory,
					  @Value("${k9sak.url}") String k9Url,
					  StsRestConsumer stsRestConsumer) {
		this.k9Url = k9Url;
		this.restTemplate = restTemplateBuilder
				.requestFactory(() -> clientHttpRequestFactory)
				.build();
		this.stsRestConsumer = stsRestConsumer;
	}


	@CircuitBreaker(name = K9_INSTANCE)
	public List<String> hentAktoerForSak(final String sakId) {
		HttpHeaders headers = createHeaders();
		ResponseEntity<List<String>> response = restTemplate.exchange(
				k9Url + "?saksnummer=" + sakId,
				GET,
				new HttpEntity<>(headers),
				new ParameterizedTypeReference<>() {}
		);

		if (OK.equals(response.getStatusCode())) {
			return response.getBody();
		} else if (BAD_REQUEST.equals(response.getStatusCode()) && response.getBody() != null) {
			throw new SafFunctionalException(String.format("hentAktoerForSak feilet funksjonelt. Feilmelding: %s", response.getBody()), response.getStatusCode());
		} else if (UNAUTHORIZED.equals(response.getStatusCode())) {
			throw new SafTechnicalException("hentAktoerForSak feilet teknisk. Tilgang avvist.", response.getStatusCode());
		} else {
			throw new SafTechnicalException("hentAktoerForSak feilet med ukjent feil i K9Consumer.");
		}
	}

	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
		headers.setBearerAuth(stsRestConsumer.getStsToken().getAccess_token());
		headers.set(NAV_CALLID, getCallId());
		return headers;
	}
}
