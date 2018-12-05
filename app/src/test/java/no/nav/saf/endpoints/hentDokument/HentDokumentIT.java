package no.nav.saf.endpoints.hentDokument;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.endpoints.AbstractItest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Base64;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class HentDokumentIT extends AbstractItest {

	private static String DOKUMENT_ID = "123";
	private static String JOURNALPOST_ID = "123";
	private static VariantFormatCode VARIANTFORMAT = VariantFormatCode.ARKIV;
	private static byte[] TEST_FILE_BYTES = "TestThis".getBytes();

	public HentDokumentIT() {
	}

	@Test
	public void hentDokumentHappyPath() {

		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost-happy.json")));

		stubFor(get("/gsak/10672720").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentsak/hentsakbysaksid-happy.json")));

		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.toString();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);

		assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
		assertEquals(responseEntity.getBody(), new String(TEST_FILE_BYTES));
	}

	@Test
	public void hentDokumentNotFound() {

		abacPermit();
		stubFor(get("/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost-happy.json")));

		stubFor(get("/gsak/10672720").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentsak/hentsakbysaksid-happy.json")));

		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.toString();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);

		assertEquals(responseEntity.getStatusCode(), HttpStatus.NOT_FOUND);
	}

	@Test
	public void hentDokumentDecodeFail() {

		byte[] decodeFailProvokerFile = "whitespace breaks base64 decode".getBytes();

		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(decodeFailProvokerFile)));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost-happy.json")));

		stubFor(get("/gsak/10672720").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentsak/hentsakbysaksid-happy.json")));


		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.toString();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);

		assertEquals(responseEntity.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@Test
	public void hentDokumentAbacDeny() {

		abacDeny();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost-happy.json")));

		stubFor(get("/gsak/10672720").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentsak/hentsakbysaksid-happy.json")));

		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.toString();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);

		assertEquals(responseEntity.getStatusCode(), HttpStatus.UNAUTHORIZED);
	}

	@Test
	public void hentDokumentJoarkFailed() {

		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost-happy.json")));

		stubFor(get("/gsak/10672720").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentsak/hentsakbysaksid-happy.json")));

		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.toString();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);

		assertEquals(responseEntity.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
