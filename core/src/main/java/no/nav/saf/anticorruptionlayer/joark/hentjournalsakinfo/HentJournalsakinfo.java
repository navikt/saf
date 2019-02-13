package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo;

import static no.nav.saf.util.MDCConstants.CORRELATION_ID;

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
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
	private final String hentjournalsakinfoUrl;

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
		this.hentjournalsakinfoUrl = hentjournalsakinfoUrl;
	}

	@Monitor(value = "dok_consumer", extraTags = {"process", "finnJournalposter"}, histogram = true)
	public FinnJournalposterResponseTo finnJournalposter(FinnJournalposterRequestTo request) {
		ResponseEntity<FinnJournalposterResponseTo> response = callFinnJournalposter(request);
		return response.getBody();
	}

	@Monitor(value = "dok_consumer", extraTags = {"process", "hentTilgangJournalpost"}, histogram = true)
	public HentTilgangJournalpostResponseTo hentTilgangJournalpost(String journalpostId, String dokumentInfoId, String variantFormat) {
		try {
			return callHentTilgangJournalpost(journalpostId, dokumentInfoId, variantFormat).getBody();
		} catch (HttpServerErrorException e) {
			throw new SafTechnicalException(String.format("henttilgangjournalpost feilet teknisk med statusKode=%s. Feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e, e.getStatusCode());
		} catch (HttpClientErrorException e) {
			switch (e.getStatusCode()) {
				case NOT_FOUND:
					throw new DokumentIkkeFunnetException(String.format("Journalpost med journalpostId=%s og tilknyttet dokumentInfoId=%s og variantFormat=%s ikke funnet i Joark.",
							journalpostId, dokumentInfoId, variantFormat));
				case BAD_REQUEST:
					throw new UgyldigInputException(String.format("Ugyldig input: journalpostId=%s, dokumentInfoId=%s, variantFormat=%s. JournalpostId og dokumentInfoId må være tall og variantFormat må være en gyldig kodeverk-verdi, eg. ARKIV, ORIGINAL, SLADDET mfl.",
							journalpostId, dokumentInfoId, variantFormat));
				default:
					throw new SafFunctionalException(String.format("hentTilgangJournalpost feilet funksjonelt. journalpostId=%s, dokumentInfoId=%s og variantFormat=%s. Feilmelding=%s",
							journalpostId, dokumentInfoId, variantFormat, e.getMessage()));
			}
		}
	}

	@Monitor(value = "dok_consumer", extraTags = {"process", "hentDokument"}, histogram = true)
	public HentDokumentResponseTo hentDokument(String dokumentInfoId, String variantFormat) {
		try {
			ResponseEntity<String> response = callHentDokument(dokumentInfoId, variantFormat);

			return HentDokumentResponseTo.builder()
					.dokument(response.getBody())
					.mediaType(response.getHeaders().getContentType())
					.build();
		} catch (HttpServerErrorException e) {
			throw new SafTechnicalException(String.format("hentDokument feilet teknisk med statusKode=%s. Feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e, e.getStatusCode());
		} catch (HttpClientErrorException e) {
			throw new DokumentIkkeFunnetException(String.format("Dokument med dokumentInfoId=%s og variantFormat=%s ikke funnet. Feilmelding=%s",
					dokumentInfoId, variantFormat, e.getMessage()));
		}
	}

	private HttpHeaders createCorrelationIdHeader() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-Correlation-ID", MDC.get(CORRELATION_ID));
		return headers;
	}

	private ResponseEntity<FinnJournalposterResponseTo> callFinnJournalposter(FinnJournalposterRequestTo requestTo) {
		String uri = hentjournalsakinfoUrl + "/finnjournalposter";
		HttpEntity<FinnJournalposterRequestTo> requestEntity = new HttpEntity<>(requestTo, createCorrelationIdHeader());
		return restTemplate.exchange(uri, HttpMethod.POST, requestEntity, FinnJournalposterResponseTo.class);
	}

	private ResponseEntity<HentTilgangJournalpostResponseTo> callHentTilgangJournalpost(String journalpostId, String dokumentInfoId, String variantFormat) {
		String uri = hentjournalsakinfoUrl + "/henttilgangjournalpost/{journalpostId}/{dokumentInfoId}/{variantFormat}";
		return restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(createCorrelationIdHeader()), HentTilgangJournalpostResponseTo.class, journalpostId, dokumentInfoId, variantFormat);
	}

	private ResponseEntity<String> callHentDokument(String dokumentInfoId, String variantFormat) {
		String uri = hentjournalsakinfoUrl + "/hentdokument/{dokumentInfoId}/{variantFormat}";
		return restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(createCorrelationIdHeader()), String.class, dokumentInfoId, variantFormat);
	}
}
