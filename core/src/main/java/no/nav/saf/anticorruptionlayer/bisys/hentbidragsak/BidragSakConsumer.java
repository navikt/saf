package no.nav.saf.anticorruptionlayer.bisys.hentbidragsak;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.config.ServiceuserAlias;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import static no.nav.saf.cache.LokalCacheConfig.BIDRAG_SAK_BY_SAKID_CACHE;

@Slf4j
@Component
public class BidragSakConsumer {
	private static final String BIDRAG_INSTANCE = "bidragsak";

	private final RestTemplate restTemplate;
	private final String bidragSakApiUrl;

	public BidragSakConsumer(RestTemplateBuilder restTemplateBuilder,
							 ClientHttpRequestFactory clientHttpRequestFactory,
							 @Value("${bidrag.sak.url}") String bidragSakApiUrl,
							 ServiceuserAlias serviceuserAlias) {
		this.bidragSakApiUrl = bidragSakApiUrl;
		this.restTemplate = restTemplateBuilder
				.requestFactory(() -> clientHttpRequestFactory)
				.basicAuthentication(serviceuserAlias.getUsername(), serviceuserAlias.getPassword()).build();
	}

	//Testing av bidrag-sak fungerer kun med passord til servicebruker i prod. Må derfor testes lokalt med hardkoding av brukernavn/passord.
	@CircuitBreaker(name = BIDRAG_INSTANCE)
	@Cacheable(cacheNames = BIDRAG_SAK_BY_SAKID_CACHE, key = "#sakId")
	public BidragSakTo hentBidragSak(final String sakId) {
		log.info("Henter relevante parter fra Bisys for sak={}", sakId);
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
