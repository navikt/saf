package no.nav.saf.anticorruptionlayer.gsak.hentgsaksaker;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.gsak.domain.GsakSakerTo;
import no.nav.saf.cache.LokalCacheConfig;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.integration.fasit.ServiceuserAlias;
import no.nav.saf.metrics.Monitor;
import no.nav.saf.tjeneste.visningsmodell.kode.Tema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class GsakConsumer {

	private final RestTemplate restTemplate;
	private final String gsakApiUrl;

	public GsakConsumer(RestTemplateBuilder restTemplateBuilder,
						@Value("${sak.saker.url}") String gsakApiUrl,
						ServiceuserAlias serviceuserAlias) {
		this.gsakApiUrl = gsakApiUrl;
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(Duration.ofSeconds(10))
				.setConnectTimeout(Duration.ofSeconds(5))
				.basicAuthentication(serviceuserAlias.getUsername(), serviceuserAlias.getPassword()).build();
	}

	@Cacheable(cacheNames = LokalCacheConfig.SAKER_BY_AKTOER_ID_CACHE, key = "#aktoerId")
	@Monitor(value = "dok_consumer", extraTags = {"process", "hentSakerByAktoerId"}, histogram = true)
	public List<GsakSakerTo> hentSakerByAktoerId(final String aktoerId) {
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(gsakApiUrl)
				.queryParam("aktoerId", aktoerId);
		return hentSaker(uri.toUriString());
	}

	@Cacheable(cacheNames = LokalCacheConfig.SAKER_BY_AKTOER_ID_CACHE, key = "#aktoerId + '_' + #tema")
	@Monitor(value = "dok_consumer", extraTags = {"process", "hentSakerByAktoerIdWithTemakode"}, histogram = true)
	public List<GsakSakerTo> hentSakerByAktoerId(final String aktoerId, final Tema tema) {
		UriComponentsBuilder uri = UriComponentsBuilder.fromHttpUrl(gsakApiUrl)
				.queryParam("aktoerId", aktoerId)
				.queryParam("tema", tema.toString());
		return hentSaker(uri.toUriString());
	}

	private List<GsakSakerTo> hentSaker(final String uri) {
		if(log.isDebugEnabled()) {
			log.debug("Henter gsaker uri={}", uri);
		}
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("X-Correlation-ID", UUID.randomUUID().toString());
			ResponseEntity<List<GsakSakerTo>> response = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<List<GsakSakerTo>>() {
			});
			return response.getBody();
		} catch (HttpServerErrorException e) {
			throw new SafTechnicalException(String.format("getGsaksaker feilet teknisk med statusKode=%s. Feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e, e.getStatusCode());
		} catch (HttpClientErrorException e) {
			throw new SafFunctionalException(String.format("getGsaksaker feilet funksjonelt med statusKode=%s. Feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e, e.getStatusCode());
		}
	}

	@Cacheable(cacheNames = LokalCacheConfig.SAK_BY_SAKID_CACHE, key = "#sakId")
	public GsakSakerTo hentSakBySakId(final String sakId) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("X-Correlation-ID", UUID.randomUUID().toString());
			return restTemplate.exchange(gsakApiUrl + "/{sakId}", HttpMethod.GET, new HttpEntity<>(headers), GsakSakerTo.class, sakId).getBody();
		} catch (HttpServerErrorException e) {
			throw new SafTechnicalException(String.format("getGsaksaker feilet teknisk med statusKode=%s. Feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e, e.getStatusCode());
		} catch (HttpClientErrorException e) {
			throw new SafFunctionalException(String.format("getGsaksaker feilet funksjonelt med statusKode=%s. Feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e, e.getStatusCode());
		}
	}
}
