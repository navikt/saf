package no.nav.saf.anticorruptionlayer.fpsak.hentRelevanteParter;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.cache.LokalCacheConfig;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.integration.sts.StsRestConsumer;
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
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;

@Slf4j
@Component
public class FpsakConsumer {
	private final String fpsakUrl;
	private final RestTemplate restTemplate;
	private final StsRestConsumer stsRestConsumer;

	public FpsakConsumer(RestTemplateBuilder restTemplateBuilder,
						 @Value("${fpsak.url}") String fpsakUrl,
						 StsRestConsumer stsRestConsumer) {
		this.fpsakUrl = fpsakUrl;
		this.stsRestConsumer = stsRestConsumer;
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(Duration.ofSeconds(20))
				.setConnectTimeout(Duration.ofSeconds(5)).build();
	}

	@Cacheable(cacheNames = LokalCacheConfig.FPSAK_RELEVANTE_PARTER_BY_SAKID_CACHE, key = "#sakId")
	public FpsakTo hentAktoerForSak(final String sakId) {
		HttpHeaders headers = createHeaders();
		final UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(fpsakUrl)
				.queryParam("saksnummer", sakId);
		ResponseEntity<FpsakTo> response = restTemplate.exchange(uri.toUriString(), HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<>() {
		});

		if (HttpStatus.OK.equals(response.getStatusCode()) && response.getBody() != null) {
			return response.getBody();
		} else if (HttpStatus.BAD_REQUEST.equals(response.getStatusCode()) && response.getBody() != null && response.getBody().getFeilmelding() != null) {
			throw new SafFunctionalException(String.format("hentAktoerForSak feilet funksjonelt. Feilmelding: %s", response.getBody().getFeilmelding()), response.getStatusCode());
		} else if (HttpStatus.UNAUTHORIZED.equals(response.getStatusCode())) {
			throw new SafTechnicalException("hentAktoerForSak feilet teknisk. Tilgang avvist.", response.getStatusCode());
		} else {
			throw new SafTechnicalException("hentAktoerForSak feilet med ukjent feil.", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + stsRestConsumer.getOidcToken());
		return headers;
	}
}
