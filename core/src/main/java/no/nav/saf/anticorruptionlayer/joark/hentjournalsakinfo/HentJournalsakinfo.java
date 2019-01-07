package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo;

import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.FinnJournalposterRequestTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.FinnJournalposterResponseTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901.HentTilgangJournalpostResponseTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark920.HentDokumentResponseTo;
import no.nav.saf.exceptions.DokumentIkkeFunnetException;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.exceptions.UgyldigInputException;
import no.nav.saf.integration.fasit.ServiceuserAlias;
import no.nav.saf.metrics.Monitor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
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

	@Monitor(value = "dok_consumer", extraTags = {"process", "finnJournalposter"}, histogram = true)
	public FinnJournalposterResponseTo finnJournalposter(FinnJournalposterRequestTo request) {
		ResponseEntity<FinnJournalposterResponseTo> response = restTemplate.postForEntity("/finnjournalposter", request, FinnJournalposterResponseTo.class);
		return response.getBody();
	}

	@Monitor(value = "dok_consumer", extraTags = {"process", "hentTilgangJournalpost"}, histogram = true)
	public HentTilgangJournalpostResponseTo hentTilgangJournalpost(String journalpostId, String dokumentId, String variantFormat) {
		try {
			return restTemplate.getForObject("/henttilgangjournalpost/{journalpostId}/{dokumentId}/{variantFormat}", HentTilgangJournalpostResponseTo.class, journalpostId, dokumentId, variantFormat);
		} catch (HttpServerErrorException e) {
			throw new SafTechnicalException(String.format("henttilgangjournalpost feilet teknisk med statusKode=%s. Feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e, e.getStatusCode());
		} catch (HttpClientErrorException e) {
			switch (e.getStatusCode()) {
				case NOT_FOUND:
					throw new DokumentIkkeFunnetException(String.format("Journalpost med journalpostId=%s og tilknyttet dokumentId=%s og variantFormat=%s ikke funnet i Joark.",
							journalpostId, dokumentId, variantFormat));
				case BAD_REQUEST:
					throw new UgyldigInputException(String.format("Ugyldig input: journalpostId=%s, dokumentId=%s, variantFormat=%s. JournalpostId og dokumentId må være tall og variantFormat må være en gyldig kodeverk-verdi, eg. ARKIV, ORIGINAL, SLADDET mfl.",
							journalpostId, dokumentId, variantFormat));
				default:
					throw new SafFunctionalException(String.format("hentTilgangJournalpost feilet funksjonelt. journalpostId=%s, dokumentId=%s og variantFormat=%s. Feilmelding=%s",
							journalpostId, dokumentId, variantFormat, e.getMessage()));
			}
		}
	}

	@Monitor(value = "dok_consumer", extraTags = {"process", "hentDokument"}, histogram = true)
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
