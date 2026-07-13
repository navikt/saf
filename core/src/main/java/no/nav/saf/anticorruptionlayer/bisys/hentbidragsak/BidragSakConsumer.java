package no.nav.saf.anticorruptionlayer.bisys.hentbidragsak;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.config.SafProperties;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import static no.nav.saf.cache.LokalCacheConfig.BIDRAG_SAK_BY_SAKID_CACHE;
import static no.nav.saf.integration.token.NaisTexasAndCallIdRequestInterceptor.TARGET_SCOPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@Component
public class BidragSakConsumer {
	private static final String BIDRAG_INSTANCE = "bidragsak";

	private final String bidragSakScope;
	private final RestClient texasAuthorizedRestClient;

	public BidragSakConsumer(RestClient texasAuthorizedRestClient, SafProperties safProperties) {
		this.texasAuthorizedRestClient = texasAuthorizedRestClient.mutate()
				.baseUrl(safProperties.getEndpoints().getBidragsak().getUrl())
				.build();
		this.bidragSakScope = safProperties.getEndpoints().getBidragsak().getScope();
	}

	@CircuitBreaker(name = BIDRAG_INSTANCE)
	@Cacheable(cacheNames = BIDRAG_SAK_BY_SAKID_CACHE, key = "#sakId")
	public BidragSakTo hentBidragSak(final String sakId) {
		log.info("Henter relevante parter fra bidrag-sak for sakId={}", sakId);

		return texasAuthorizedRestClient.get()
				.uri(uriBuilder -> uriBuilder.path("/v2/pip/sak/{sakId}").build(sakId))
				.attribute(TARGET_SCOPE, bidragSakScope)
				.exchange((_, clientResponse) ->
						switch (clientResponse.getStatusCode()) {
							case OK -> clientResponse.bodyTo(BidragSakTo.class);
							case NO_CONTENT ->
									throw new SafFunctionalException(String.format("hentBidragSak fikk tilbake tom respons. Ingen innslag funnet på sakId=%s", sakId));
							case BAD_REQUEST ->
									throw new SafTechnicalException(String.format("hentBidragSak feilet. SakId=%s er ikke 7 tegn", sakId),
											clientResponse.getStatusCode());
							case NOT_FOUND ->
									throw new SafTechnicalException(String.format("hentBidragSak kunne ikke kontakte bidrag-pip. sakId=%s", sakId),
											clientResponse.getStatusCode());
							default ->
									throw new SafTechnicalException(String.format("hentBidragSak feilet teknisk med statusKode=%s. Responsebody=%s",
											clientResponse.getStatusCode(), clientResponse.bodyTo(String.class)), clientResponse.getStatusCode());
						});
	}
}
