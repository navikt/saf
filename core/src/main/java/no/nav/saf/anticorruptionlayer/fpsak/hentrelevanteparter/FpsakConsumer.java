package no.nav.saf.anticorruptionlayer.fpsak.hentrelevanteparter;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import no.nav.saf.anticorruptionlayer.sts.StsRestConsumer;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;

import static java.time.Duration.ofSeconds;
import static no.nav.saf.cache.LokalCacheConfig.FPSAK_RELEVANTE_PARTER_BY_SAKID_CACHE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class FpsakConsumer {
	private static final String FPSAK_INSTANCE = "fpsak";

	private final String fpsakUrl;
	private final RestTemplate restTemplate;
	private final StsRestConsumer stsRestConsumer;

	public FpsakConsumer(RestTemplateBuilder restTemplateBuilder,
						 ClientHttpRequestFactory clientHttpRequestFactory,
						 @Value("${fpsak.url}") String fpsakUrl,
						 StsRestConsumer stsRestConsumer) {
		this.fpsakUrl = fpsakUrl;
		this.restTemplate = restTemplateBuilder
				.requestFactory(() -> clientHttpRequestFactory)
				.build();
		this.stsRestConsumer = stsRestConsumer;
	}

	@CircuitBreaker(name = FPSAK_INSTANCE)
	@Cacheable(cacheNames = FPSAK_RELEVANTE_PARTER_BY_SAKID_CACHE, key = "#sakId")
	public List<String> hentAktoerForSak(final String sakId) {
		HttpHeaders headers = createHeaders();
		ResponseEntity<List<String>> response = restTemplate.exchange(fpsakUrl + "?saksnummer=" + sakId, GET, new HttpEntity<>(headers), new ParameterizedTypeReference<>() {
		});

		if (OK.equals(response.getStatusCode())) {
			return response.getBody();
		} else if (BAD_REQUEST.equals(response.getStatusCode()) && response.getBody() != null) {
			throw new SafFunctionalException(String.format("hentAktoerForSak feilet funksjonelt. Feilmelding: %s", response.getBody()), response.getStatusCode());
		} else if (UNAUTHORIZED.equals(response.getStatusCode())) {
			throw new SafTechnicalException("hentAktoerForSak feilet teknisk. Tilgang avvist.", response.getStatusCode());
		} else {
			throw new SafTechnicalException("hentAktoerForSak feilet med ukjent feil.", response.getStatusCode());
		}
	}

	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
		headers.setBearerAuth(stsRestConsumer.getStsToken().getAccess_token());
		return headers;
	}
}
