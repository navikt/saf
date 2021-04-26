package no.nav.saf.integration.sts;


import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import no.nav.saf.cache.LokalCacheConfig;
import no.nav.saf.config.ServiceuserAlias;
import no.nav.saf.exceptions.StsTechnicalException;
import no.nav.saf.metrics.Monitor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.time.Duration;


@Component
public class StsRestConsumer {

	private static final String STS_INSTANCE = "sts";

	private final RestTemplate restTemplate;
	private final String stsUrl;

	@Inject
	public StsRestConsumer(@Value("${security-token-service-token.url}") String stsUrl, RestTemplateBuilder restTemplateBuilder,
						   final ServiceuserAlias serviceuserAlias) {
		this.stsUrl = stsUrl;
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(Duration.ofSeconds(20))
				.setConnectTimeout(Duration.ofSeconds(5))
				.basicAuthentication(serviceuserAlias.getUsername(), serviceuserAlias.getPassword())
				.build();
	}

	@Retry(name = STS_INSTANCE)
	@Cacheable(cacheNames = LokalCacheConfig.STS_REST_CACHE)
	public String getOidcToken() {
		try {
			StsResponseTo stsResponseTo = restTemplate.getForObject(stsUrl + "?grant_type=client_credentials&scope=openid", StsResponseTo.class);
			return stsResponseTo != null ? stsResponseTo.getAccessToken() : null;
		} catch (HttpStatusCodeException e) {
			throw new StsTechnicalException(String.format("Kall mot STS feilet med status=%s feilmelding=%s.", e.getStatusCode(), e
					.getMessage()), e);
		} catch (ResourceAccessException e) {
			throw new StsTechnicalException(String.format("Kall mot STS feilet med manglende tilgang: %s", e.getMessage()), e);
		}
	}
}
