package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo;

import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.FinnJournalposterRequestTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.FinnJournalposterResponseTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901.HentTilgangJournalpostResponseTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark902.HentJournalpostResponseTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark903.TilknytningUriParam;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark903.TilknyttedeJournalposterResponse;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark904.FinnJournalposterStatusRequestTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark904.FinnJournalposterStatusResponseTo;
import no.nav.saf.config.ServiceuserAlias;
import no.nav.saf.exceptions.DokumentIkkeFunnetException;
import no.nav.saf.exceptions.JournalpostIkkeFunnetException;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.exceptions.UgyldigInputException;
import no.nav.saf.metrics.Monitor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static no.nav.saf.headers.NavHeaders.NAV_CALLID;
import static no.nav.saf.util.MDCConstants.CALL_ID;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class HentJournalsakinfo {
	private static final String JOARK_HENTDOKUMENT = "joarkhentdokument";

	private final RestTemplate restTemplate;
	private final String hentjournalsakinfoUrl;

	@Autowired
	public HentJournalsakinfo(@Value("${hentjournalsakinfo.url}") final String hentjournalsakinfoUrl,
							  final RestTemplateBuilder restTemplateBuilder,
							  final ClientHttpRequestFactory hentJournalsakInfoClientHttpRequestFactory,
							  final ServiceuserAlias serviceuserAlias) {
		restTemplate = restTemplateBuilder
				.requestFactory(() -> hentJournalsakInfoClientHttpRequestFactory)
				.rootUri(hentjournalsakinfoUrl)
				.basicAuthentication(serviceuserAlias.getUsername(), serviceuserAlias.getPassword())
				.build();
		this.hentjournalsakinfoUrl = hentjournalsakinfoUrl;
	}

	@Monitor(value = "dok_consumer", extraTags = {"process", "finnJournalposter"}, histogram = true)
	public FinnJournalposterResponseTo finnJournalposter(FinnJournalposterRequestTo request) {
		ResponseEntity<FinnJournalposterResponseTo> response = callFinnJournalposter(request);
		return response.getBody();
	}

	@Monitor(value = "dok_consumer", extraTags = {"process", "finnJournalposterStatus"}, histogram = true)
	public FinnJournalposterStatusResponseTo finnJournalposterStatus(FinnJournalposterStatusRequestTo request) {
		ResponseEntity<FinnJournalposterStatusResponseTo> response = callFinnJournalposterStatus(request);
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
			switch (e.getStatusCode().value()) {
				case 404: // NOT_FOUND
					throw new DokumentIkkeFunnetException(String.format("Journalpost med journalpostId=%s og tilknyttet dokumentInfoId=%s og variantFormat=%s ikke funnet i Joark.",
							journalpostId, dokumentInfoId, variantFormat));
				case 400: //BAD_REQUEST
					throw new UgyldigInputException(String.format("Ugyldig input: journalpostId=%s, dokumentInfoId=%s, variantFormat=%s. JournalpostId og dokumentInfoId må være tall og variantFormat må være en gyldig kodeverk-verdi, eg. ARKIV, ORIGINAL, SLADDET mfl.",
							journalpostId, dokumentInfoId, variantFormat));
				default:
					throw new SafFunctionalException(String.format("hentTilgangJournalpost feilet funksjonelt. journalpostId=%s, dokumentInfoId=%s og variantFormat=%s. Feilmelding=%s",
							journalpostId, dokumentInfoId, variantFormat, e.getMessage()));
			}
		}
	}

	@Monitor(value = "dok_consumer", extraTags = {"process", "hentJournalpost"}, histogram = true)
	public HentJournalpostResponseTo hentJournalpost(final String journalpostId) {
		try {
			String uri = hentjournalsakinfoUrl + "/hentjournalpost/{journalpostId}";
			return restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(createNavHeaders()), HentJournalpostResponseTo.class, journalpostId).getBody();
		} catch (HttpServerErrorException e) {
			throw new SafTechnicalException(String.format("Henting av journalpostId=%s feilet teknisk. Status=%s. Feilmelding=%s",
					journalpostId, e.getStatusCode(), e.getMessage()), e, e.getStatusCode());
		} catch (HttpClientErrorException e) {
			if (e.getStatusCode().value() == NOT_FOUND.value()) {
				throw new JournalpostIkkeFunnetException("Journalpost med journalpostId=" + journalpostId + " ikke funnet.");
			}
			throw new SafFunctionalException(String.format("Henting av journalpostId=%s feilet funksjonelt. Status=%s. Feilmelding=%s",
					journalpostId, e.getStatusCode(), e.getMessage()), e, e.getStatusCode());
		} catch (RestClientException e) {
			throw new SafTechnicalException(String.format("Henting av journalpostId=%s feilet med ukjent teknisk feil.", journalpostId), e);
		}
	}

	@Monitor(value = "dok_consumer", extraTags = {"process", "tilknyttedeJournalposter"}, histogram = true)
	public TilknyttedeJournalposterResponse tilknyttedeJournalposter(final String dokumentInfoId, final TilknytningUriParam tilknytning) {
		try {
			String uri = hentjournalsakinfoUrl + "/tilknyttedejournalposter/{dokumentInfoId}/{tilknytning}";
			return restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(createNavHeaders()), TilknyttedeJournalposterResponse.class, dokumentInfoId, tilknytning.name()).getBody();
		} catch (HttpServerErrorException e) {
			throw new SafTechnicalException(String.format("tilknyttedeJournalposter feilet teknisk med statusKode=%s. Feilmelding=%s", e
					.getStatusCode(), e.getMessage()), e, e.getStatusCode());
		} catch (HttpClientErrorException e) {
			if (e.getStatusCode().value() == NOT_FOUND.value()) {
				throw new JournalpostIkkeFunnetException("Tilknyttede Journalposter for dokumentInfoId=" + dokumentInfoId + " ikke funnet.");
			}
			throw new SafFunctionalException(String.format("tilknyttedeJournalposter feilet funksjonelt. dokumentInfoId=%s, feilmelding=%s", dokumentInfoId, e.getMessage()));
		}
	}

	private ResponseEntity<FinnJournalposterResponseTo> callFinnJournalposter(FinnJournalposterRequestTo requestTo) {
		String uri = hentjournalsakinfoUrl + "/finnjournalposter";
		HttpEntity<FinnJournalposterRequestTo> requestEntity = new HttpEntity<>(requestTo, createNavHeaders());
		return restTemplate.exchange(uri, HttpMethod.POST, requestEntity, FinnJournalposterResponseTo.class);
	}

	private ResponseEntity<FinnJournalposterStatusResponseTo> callFinnJournalposterStatus(FinnJournalposterStatusRequestTo requestTo) {
		String uri = hentjournalsakinfoUrl + "/finnjournalposterstatus";
		HttpEntity<FinnJournalposterStatusRequestTo> requestEntity = new HttpEntity<>(requestTo, createNavHeaders());
		return restTemplate.exchange(uri, HttpMethod.POST, requestEntity, FinnJournalposterStatusResponseTo.class);
	}

	private ResponseEntity<HentTilgangJournalpostResponseTo> callHentTilgangJournalpost(String journalpostId, String dokumentInfoId, String variantFormat) {
		String uri = hentjournalsakinfoUrl + "/henttilgangjournalpost/{journalpostId}/{dokumentInfoId}/{variantFormat}";
		return restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(createNavHeaders()), HentTilgangJournalpostResponseTo.class, journalpostId, dokumentInfoId, variantFormat);
	}

	private ResponseEntity<String> callHentDokument(String dokumentInfoId, String variantFormat) {
		String uri = hentjournalsakinfoUrl + "/hentdokument/{dokumentInfoId}/{variantFormat}";
		return restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(createNavHeaders()), String.class, dokumentInfoId, variantFormat);
	}

	private HttpHeaders createNavHeaders() {
		HttpHeaders headers = new HttpHeaders();
		if (MDC.get(CALL_ID) != null) {
			headers.set(NAV_CALLID, MDC.get(CALL_ID));
		}
		return headers;
	}
}
