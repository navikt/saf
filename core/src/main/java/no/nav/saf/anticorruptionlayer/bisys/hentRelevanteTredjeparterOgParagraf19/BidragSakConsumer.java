package no.nav.saf.anticorruptionlayer.bisys.hentRelevanteTredjeparterOgParagraf19;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.cache.LokalCacheConfig;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.integration.fasit.ServiceuserAlias;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Slf4j
@Component
public class BidragSakConsumer {

	private final RestTemplate restTemplate;
	private final String bidragSakApiUrl;

	public BidragSakConsumer(RestTemplateBuilder restTemplateBuilder,
							 @Value("${bidrag.sak.url}") String bidragSakApiUrl,
							 ServiceuserAlias serviceuserAlias) {
		this.bidragSakApiUrl = bidragSakApiUrl;
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(Duration.ofSeconds(20))
				.setConnectTimeout(Duration.ofSeconds(5))
				.basicAuthentication(serviceuserAlias.getUsername(), serviceuserAlias.getPassword()).build();
	}


	@Cacheable(cacheNames = LokalCacheConfig.BIDRAG_SAK_BY_SAKID_CACHE, key = "#sakId")
	public BidragSakTo hentBidragSak(final String sakId) {
		try {
			return restTemplate.getForObject(bidragSakApiUrl + "/{sakId}", BidragSakTo.class, sakId);
		} catch (HttpServerErrorException e) {
			throw new SafTechnicalException(String.format("hentBidragSakBySakId feilet teknisk med statusKode=%s. Feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e, e.getStatusCode());
		} catch (HttpClientErrorException e) {
			throw new SafFunctionalException(String.format("hentBidragSakBySakId feilet funksjonelt med statusKode=%s. Feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e, e.getStatusCode());
		}
	}
}
