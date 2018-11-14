package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo;

import static no.nav.saf.cache.LokalCacheConfig.HENT_JOURNALPOSTER_CACHE;
import static no.nav.saf.cache.LokalCacheConfig.HENT_TILGANG_JOURNALPOSTER_CACHE;

import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.TilgangJournalpostBulkRequestTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.TilgangJournalpostBulkResponseTo;
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
				.rootUri(hentjournalsakinfoUrl)
				.basicAuthorization(serviceuserAlias.getUsername(), serviceuserAlias.getPassword())
				.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(5))
				.setReadTimeout((int) TimeUnit.SECONDS.toMillis(20))
				.build();
	}

	@Cacheable(cacheNames = HENT_JOURNALPOSTER_CACHE)
	public HentJournalposterResponse hentJournalposter(HentJournalposterRequest request) {
		ResponseEntity<HentJournalposterResponse> response = restTemplate.postForEntity("/hentjournalposter", request, HentJournalposterResponse.class);
		return response.getBody();
	}

	@Cacheable(cacheNames = HENT_TILGANG_JOURNALPOSTER_CACHE, key = "#request.aktoerId")
	public TilgangJournalpostBulkResponseTo hentTilgangJournalpostBulk(TilgangJournalpostBulkRequestTo request) {
		ResponseEntity<TilgangJournalpostBulkResponseTo> response = restTemplate.postForEntity("/tilgangjournalpostbulk", request, TilgangJournalpostBulkResponseTo.class);
		return response.getBody();
	}
}
