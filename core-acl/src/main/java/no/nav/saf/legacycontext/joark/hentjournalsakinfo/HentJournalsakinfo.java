package no.nav.saf.legacycontext.joark.hentjournalsakinfo;

import static no.nav.saf.cache.CacheConfig.HENT_JOURNALPOSTER_CACHE;

import no.nav.saf.integration.fasit.ServiceuserAlias;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Service
public class HentJournalsakinfo {
	private final RestTemplate restTemplate;
	private final String hentjournalsakinfoUrl;

	@Inject
	public HentJournalsakinfo(@Value("${hentjournalsakinfo.url}") final String hentjournalsakinfoUrl,
							  final RestTemplateBuilder restTemplateBuilder,
							  final ClientHttpRequestFactory clientHttpRequestFactory,
							  final ServiceuserAlias serviceuserAlias) {
		this.hentjournalsakinfoUrl = hentjournalsakinfoUrl;
		restTemplate = restTemplateBuilder
				.requestFactory(() -> clientHttpRequestFactory)
				.basicAuthorization(serviceuserAlias.getUsername(), serviceuserAlias.getPassword())
				.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(5))
				.setReadTimeout((int) TimeUnit.SECONDS.toMillis(10))
				.build();
	}

	@Cacheable(cacheNames = HENT_JOURNALPOSTER_CACHE)
	public HentJournalposterResponse hentJournalposter(HentJournalposterRequest request) {
		ResponseEntity<HentJournalposterResponse> response = restTemplate.postForEntity(hentjournalsakinfoUrl, request, HentJournalposterResponse.class);
		return response.getBody();
	}
}
