package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo;

import static no.nav.saf.cache.LokalCacheConfig.HENT_TILGANG_JOURNALPOST_CACHE;

import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.HentJournalpostBulkRequestTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.HentJournalpostBulkResponseTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901.HentTilgangJournalpostResponseTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark920.HentDokumentResponseTo;
import no.nav.saf.exceptions.DokumentIkkeFunnetException;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.integration.fasit.ServiceuserAlias;
import no.nav.saf.metrics.Monitor;
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
import java.time.Duration;

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
				.basicAuthentication(serviceuserAlias.getUsername(), serviceuserAlias.getPassword())
				.setReadTimeout(Duration.ofSeconds(60))
				.setConnectTimeout(Duration.ofSeconds(5))
				.build();
	}

	@Monitor(value = "dok_consumer", extraTags = {"process", "hentJournalpostBulk"}, histogram = true)
	public HentJournalpostBulkResponseTo hentJournalpostBulk(HentJournalpostBulkRequestTo request) {
		ResponseEntity<HentJournalpostBulkResponseTo> response = restTemplate.postForEntity("/hentjournalpostbulk", request, HentJournalpostBulkResponseTo.class);
		return response.getBody();
	}

	@Cacheable(cacheNames = HENT_TILGANG_JOURNALPOST_CACHE)
	@Monitor(value = "dok_consumer", extraTags = {"process", "hentTilgangJournalpost"}, histogram = true)
	public HentTilgangJournalpostResponseTo hentTilgangJournalpost(String journalpostId, String dokumentId, String variantFormat) {
		return restTemplate.getForObject("/henttilgangjournalpost/{journalpostId}/{dokumentId}/{variantFormat}", HentTilgangJournalpostResponseTo.class, journalpostId, dokumentId, variantFormat);
	}

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
