package no.nav.saf.context.gsak.hentgsaksaker;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.context.exceptions.SafFunctionalException;
import no.nav.saf.context.exceptions.SafTechnicalException;
import no.nav.saf.context.gsak.domain.GsakSakerTo;
import no.nav.saf.fasit.ServiceuserAlias;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
class GsakConsumer {

	private static final int TIMEOUT = 30_000;
	private final RestTemplate restTemplate;
	private final String gsakApiUrl;

	public GsakConsumer(RestTemplateBuilder restTemplateBuilder,
						@Value("${sak.saker.url}") String gsakApiUrl,
						ServiceuserAlias serviceuserAlias) {
		this.gsakApiUrl = gsakApiUrl;
		this.restTemplate = restTemplateBuilder
				.setReadTimeout(TIMEOUT)
				.setConnectTimeout(TIMEOUT)
				.basicAuthorization(serviceuserAlias.getUsername(), serviceuserAlias.getPassword()).build();
	}


	public GsakSakerTo getGsakSaker(final String saksId) {
		try {
			return restTemplate.getForObject(this.gsakApiUrl + "/" + saksId, GsakSakerTo.class);
		} catch (HttpServerErrorException e) {
			throw new SafTechnicalException(String.format("getGsaksaker feilet teknisk med statusKode=%s. Feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e, e.getStatusCode());
		} catch (HttpClientErrorException e) {
			throw new SafFunctionalException(String.format("getGsaksaker feilet funksjonelt med statusKode=%s. Feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e, e.getStatusCode());
		}
	}
}