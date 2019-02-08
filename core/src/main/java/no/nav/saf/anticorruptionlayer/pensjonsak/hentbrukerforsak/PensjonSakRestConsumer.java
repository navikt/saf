package no.nav.saf.anticorruptionlayer.pensjonsak.hentbrukerforsak;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.cache.LokalCacheConfig;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.integration.fasit.ServiceuserAlias;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Slf4j
@Component
public class PensjonSakRestConsumer {

	private final RestTemplate restTemplate;
	private final String pensjonsakApiUrl;

	public PensjonSakRestConsumer(RestTemplateBuilder restTemplateBuilder,
								  @Value("${pensjonsakrs.v1.url}") String pensjonsakApiUrl,
								  ServiceuserAlias serviceuserAlias) {
		this.pensjonsakApiUrl = pensjonsakApiUrl;
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(Duration.ofSeconds(20))
				.setConnectTimeout(Duration.ofSeconds(5))
				.basicAuthentication(serviceuserAlias.getUsername(), serviceuserAlias.getPassword()).build();
	}

	@Cacheable(cacheNames = LokalCacheConfig.SAK_BY_SAKID_CACHE, key = "#sakId")
	public HentBrukerForSakResponseTo hentBrukerForSak(final String sakId) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.add("sakId", sakId);
			HentBrukerForSakResponseTo hentBrukerForSakResponseTo = restTemplate.exchange(pensjonsakApiUrl, HttpMethod.GET, new HttpEntity<>(headers), HentBrukerForSakResponseTo.class)
					.getBody();
			if (hentBrukerForSakResponseTo.getFnr() == null || hentBrukerForSakResponseTo.getFnr().isEmpty()) {
				throw new SafFunctionalException(String.format("hentBrukerForSak returnerte tomt fødselsnummer for sakId=%s. Dette betyr at saken ikke finnes eller at ingen personer er tilknyttet denne saken", sakId));
			} else {
				return hentBrukerForSakResponseTo;
			}
		} catch (HttpServerErrorException e) {
			throw new SafTechnicalException(String.format("hentBrukerForSak feilet teknisk med statusKode=%s. Feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e, e.getStatusCode());
		} catch (HttpClientErrorException e) {
			throw new SafFunctionalException(String.format("hentBrukerForSak feilet funksjonelt med statusKode=%s. Feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e, e.getStatusCode());
		}
	}
}
