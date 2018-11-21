package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo;

import static no.nav.saf.cache.LokalCacheConfig.HENT_JOURNALPOSTER_CACHE;
import static no.nav.saf.cache.LokalCacheConfig.HENT_TILGANG_JOURNALPOSTER_CACHE;
import static no.nav.saf.cache.LokalCacheConfig.HENT_TILGANG_JOURNALPOST_CACHE;
import static no.nav.saf.cache.LokalCacheConfig.HENT_VISNING_JOURNALPOSTER_CACHE;

import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.HentJournalpostBulkRequestTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.HentJournalpostBulkResponseTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901.HentTilgangJournalpostResponseTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark910.VisningJournalpostBulkRequestTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark910.VisningJournalpostBulkResponseTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark920.HentDokumentResponseTo;
import no.nav.saf.exceptions.DokumentIkkeFunnetException;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.integration.fasit.ServiceuserAlias;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Service
public class HentJournalsakinfo {
	private final RestTemplate restTemplate;

	@Inject
	public HentJournalsakinfo(@Value("${hentjournalsakinfo.url}") final String hentjournalsakinfoUrl,
							  final RestTemplateBuilder restTemplateBuilder,
							  final ClientHttpRequestFactory clientHttpRequestFactory,
							  final ServiceuserAlias serviceuserAlias) {
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

	@Cacheable(cacheNames = HENT_TILGANG_JOURNALPOSTER_CACHE)
	public HentJournalpostBulkResponseTo hentJournalpostBulk(HentJournalpostBulkRequestTo request) {
		ResponseEntity<HentJournalpostBulkResponseTo> response = restTemplate.postForEntity("/hentjournalpostbulk", request, HentJournalpostBulkResponseTo.class);
		return response.getBody();
	}

	@Cacheable(cacheNames = HENT_VISNING_JOURNALPOSTER_CACHE, key = "#request.journalpostIds")
	public VisningJournalpostBulkResponseTo hentVisningJournalpostBulk(VisningJournalpostBulkRequestTo request) {
		ResponseEntity<VisningJournalpostBulkResponseTo> response = restTemplate.postForEntity("/visningjournalpostbulk", request, VisningJournalpostBulkResponseTo.class);
		return response.getBody();
	}

	@Cacheable(cacheNames = HENT_TILGANG_JOURNALPOST_CACHE)
	public HentTilgangJournalpostResponseTo hentTilgangJournalpost(String journalpostId, String dokumentId, String variantFormat) {
		return restTemplate.getForObject("/henttilgangjournalpost/{journalpostId}/{dokumentId}/{variantFormat}", HentTilgangJournalpostResponseTo.class, journalpostId, dokumentId, variantFormat);
	}

	//TODO Skal vi chache dokumenter?
	public HentDokumentResponseTo hentDokument(String dokumentId, String variantFormat) {
		try {
			ResponseEntity<String> response = restTemplate.getForEntity("/hentdokument/{dokumentId}/{variantFormat}", String.class, dokumentId, variantFormat);
			return HentDokumentResponseTo.builder()
					.dokument(response.getBody())
					.mediaType(response.getHeaders().getContentType())
					.build();
		} catch (HttpServerErrorException e) {
			throw new SafTechnicalException(String.format("hentDokument feilet teknisk med statusKode=%s. Feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e, e.getStatusCode());
		} catch (HttpClientErrorException e) {
			throw new DokumentIkkeFunnetException(String.format("Dokument med dokumentId=%s og variantFormat=%s ikke funnet. Feilmelding=%s",
					dokumentId, variantFormat, e.getMessage()));
		}
	}
}
