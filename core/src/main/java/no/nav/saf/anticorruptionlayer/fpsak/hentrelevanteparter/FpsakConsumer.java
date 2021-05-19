package no.nav.saf.anticorruptionlayer.fpsak.hentrelevanteparter;

import no.nav.saf.anticorruptionlayer.sts.StsRestConsumer;
import no.nav.saf.cache.LokalCacheConfig;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;

@Component
public class FpsakConsumer {
	private final String fpsakUrl;
	private final RestTemplate restTemplate;
	private final StsRestConsumer stsRestConsumer;

	public FpsakConsumer(RestTemplateBuilder restTemplateBuilder,
						 @Value("${fpsak.url}") String fpsakUrl,
						 StsRestConsumer stsRestConsumer) {
		this.fpsakUrl = fpsakUrl;
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(Duration.ofSeconds(20))
				.setConnectTimeout(Duration.ofSeconds(5)).build();
		this.stsRestConsumer = stsRestConsumer;
	}

	@Cacheable(cacheNames = LokalCacheConfig.FPSAK_RELEVANTE_PARTER_BY_SAKID_CACHE, key = "#sakId")
	public List<String> hentAktoerForSak(final String sakId) {
		HttpHeaders headers = createHeaders();
		ResponseEntity<List<String>> response = restTemplate.exchange(fpsakUrl + "?saksnummer=" + sakId, HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<List<String>>() {
		});

		if (HttpStatus.OK.equals(response.getStatusCode())) {
			return response.getBody();
		} else if (HttpStatus.BAD_REQUEST.equals(response.getStatusCode()) && response.getBody() != null) {
			throw new SafFunctionalException(String.format("hentAktoerForSak feilet funksjonelt. Feilmelding: %s", response.getBody()), response.getStatusCode());
		} else if (HttpStatus.UNAUTHORIZED.equals(response.getStatusCode())) {
			throw new SafTechnicalException("hentAktoerForSak feilet teknisk. Tilgang avvist.", response.getStatusCode());
		} else {
			throw new SafTechnicalException("hentAktoerForSak feilet med ukjent feil.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + stsRestConsumer.getStsToken().getAccess_token());
		return headers;
	}
}
