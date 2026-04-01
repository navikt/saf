package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo;

import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.FinnJournalposterRequestTo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.FinnJournalposterResponseTo;
import no.nav.saf.config.ServiceuserAlias;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestTemplate;

import static no.nav.saf.headers.NavHeaders.NAV_CALLID;
import static no.nav.saf.util.MDCConstants.CALL_ID;


@Service
@Validated
public class HentJournalsakinfo {

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

	public FinnJournalposterResponseTo finnJournalposter(FinnJournalposterRequestTo request) {
		ResponseEntity<FinnJournalposterResponseTo> response = callFinnJournalposter(request);
		return response.getBody();
	}

	private ResponseEntity<FinnJournalposterResponseTo> callFinnJournalposter(FinnJournalposterRequestTo requestTo) {
		String uri = hentjournalsakinfoUrl + "/finnjournalposter";
		HttpEntity<FinnJournalposterRequestTo> requestEntity = new HttpEntity<>(requestTo, createNavHeaders());
		return restTemplate.exchange(uri, HttpMethod.POST, requestEntity, FinnJournalposterResponseTo.class);
	}

	private HttpHeaders createNavHeaders() {
		HttpHeaders headers = new HttpHeaders();
		if (MDC.get(CALL_ID) != null) {
			headers.set(NAV_CALLID, MDC.get(CALL_ID));
		}
		return headers;
	}
}
