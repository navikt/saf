package no.nav.saf.endpoints.hentDokument;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

import jdk.nashorn.internal.ir.annotations.Ignore;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901.HentTilgangJournalpostResponseTo;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Base64;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class HentDokumentIT extends AbstractEndpointEvaluatorIT {

	private static String DOKUMENT_ID = "441360260";
	private static String JOURNALPOST_ID = "123";
	private static VariantFormatCode VARIANTFORMAT = VariantFormatCode.ARKIV;
	private static byte[] TEST_FILE_BYTES = "TestThis".getBytes();

	public HentDokumentIT() {
	}

	@Ignore
	@Test
	public void hentDokumentHappyPath() {

		abacPermit();
		stubFor(get("/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

//Uncomment when database response has been found, also uncomment HentDokumentDomainCoordinatorImpl
//		stubFor(get("/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
//				.willReturn().withStatus(HttpStatus.OK.value()));

		restTemplate.getForObject("/henttilgangjournalpost/{journalpostId}/{dokumentId}/{variantFormat}", HentTilgangJournalpostResponseTo.class, JOURNALPOST_ID, DOKUMENT_ID, VARIANTFORMAT);

		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.toString();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHeaders(), String.class);

		assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
		assertEquals(responseEntity.getBody(), new String(TEST_FILE_BYTES));
	}

	@Test
	public void hentDokumentNotFound() {

		abacPermit();
		stubFor(get("/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.toString();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHeaders(), String.class);

		assertEquals(responseEntity.getStatusCode(), HttpStatus.NOT_FOUND);
	}

	@Test
	public void hentDokumentDecodeFail() {
		abacPermit();
		stubFor(get("/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(TEST_FILE_BYTES)));

		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.toString();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHeaders(), String.class);

		assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
		assertThat(responseEntity.getBody(), not(new String(TEST_FILE_BYTES)));
	}

//	@Test
//	public void hentDokumentJoarkFailed() {

//		abacPermit();
//		stubFor(get("/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK.value())
//				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
//				.withBody(testFile)));
//
//		//Uncomment when database response has been found, also uncomment HentDokumentDomainCoordinatorImpl
////		stubFor(get("/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
////				.willReturn().withStatus(HttpStatus.OK.value()));
//
////		restTemplate.getForObject("/henttilgangjournalpost/{journalpostId}/{dokumentId}/{variantFormat}", HentTilgangJournalpostResponseTo.class, journalpostId, dokumentId, variantFormat)
//
//		String uri = "/rest/hentdokument/" + journalpostId + "/" + dokumentId + "/" + variantFormat.toString();
//		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHeaders(), String.class);
//
//		assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
//		assertThat(responseEntity.getBody(),not(new String(testFile)));
//
//	}

	// Fix when peps are uncommented.
	@Test
	public void hentDokumentAbacDeny() {

		abacDeny();
		stubFor(get("/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(TEST_FILE_BYTES)));

		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.toString();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHeaders(), String.class);

		assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
//		assertThat(responseEntity.getBody(), not(new String(TEST_FILE_BYTES)));

	}
}
