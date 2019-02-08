package no.nav.saf.anticorruptionlayer.bisys.hentbidragsak;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.cache.LokalCacheConfig;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.integration.fasit.ServiceuserAlias;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
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
		ResponseEntity<BidragSakTo> response = restTemplate.getForEntity(bidragSakApiUrl + "/{sakId}", BidragSakTo.class, sakId);
		switch (response.getStatusCode()) {
			case OK:
				return response.getBody();
			case NO_CONTENT:
				throw new SafFunctionalException(String.format("hentBidragSak fikk tilbake tom respons. Ingen innslag funnet på sakId=%s", sakId));
			case NOT_FOUND:
				throw new SafTechnicalException(String.format("hentBidragSak kunne ikke kontakte bidrag-pip. sakId=%s", sakId), response
						.getStatusCode());
			case BAD_REQUEST:
				throw new SafTechnicalException(String.format("hentBidragSak feilet. SakId=%s er ikke 7 tegn", sakId), response
						.getStatusCode());
			default:
				throw new SafTechnicalException(String.format("hentBidragSak feilet teknisk med statusKode=%s. Responsebody=%s", response
						.getStatusCode(), response.getBody()), response.getStatusCode());
		}
	}
}
