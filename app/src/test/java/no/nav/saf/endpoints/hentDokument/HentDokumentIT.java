package no.nav.saf.endpoints.hentDokument;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
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
	private static String SCENARIO_HENTSAK = "scenario_hent_sak";
	private static String STATE_TILGANG_SAK = "state_tilgangSAK";
	private static byte[] TEST_FILE_BYTES = "TestThis".getBytes();

	public HentDokumentIT() {
	}

	@Test
	public void hentGsakDokumentHappyPath() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_gsak-happy.json")));

		stubFor(get("/gsak/10672720").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentsak/hentsakbysaksid-happy.json")));

		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.name();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(new String(TEST_FILE_BYTES), responseEntity.getBody());

		verify(getRequestedFor(urlEqualTo("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)).withBasicAuth(new BasicCredentials("srvsaf", "srvsafpw")));
		verify(getRequestedFor(urlEqualTo("/gsak/10672720")).withBasicAuth(new BasicCredentials("srvsaf", "srvsafpw")));
	}

	@Test
	public void hentGsakDokumentHappyPathBrukerOrganisasjon() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_gsak-happy.json")));

		stubFor(get("/gsak/10672720").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentsak/hentsakbysaksid_bruker_organisasjon-happy.json")));

		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.name();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(new String(TEST_FILE_BYTES), responseEntity.getBody());

		verify(getRequestedFor(urlEqualTo("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)).withBasicAuth(new BasicCredentials("srvsaf", "srvsafpw")));
		verify(getRequestedFor(urlEqualTo("/gsak/10672720")).withBasicAuth(new BasicCredentials("srvsaf", "srvsafpw")));
		verify(0, getRequestedFor(urlEqualTo("/abac")));
	}


	@Test
	public void hentPsakDokumentHappyPath() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_psak-happy.json")));

		stubFor(get("/pensjonsakrs").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("psak/psak-hentBrukerForSak-happy.json")));

		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.name();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(new String(TEST_FILE_BYTES), responseEntity.getBody());

		verify(getRequestedFor(urlEqualTo("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)).withBasicAuth(new BasicCredentials("srvsaf", "srvsafpw")));
		verify(getRequestedFor(urlEqualTo("/pensjonsakrs")).withBasicAuth(new BasicCredentials("srvsaf", "srvsafpw")));
		verify(getRequestedFor(urlEqualTo("/pensjonsakrs")).withHeader("sakId", equalTo("10672720")));
	}

	@Test
	public void hentMidlertidigDokumentHappyPath() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_midlertidig-happy.json")));

		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.name();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(new String(TEST_FILE_BYTES), responseEntity.getBody());

		verify(getRequestedFor(urlEqualTo("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)).withBasicAuth(new BasicCredentials("srvsaf", "srvsafpw")));
	}

	@Test
	public void hentBrukerForSakTechnicalError() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_psak-happy.json")));

		stubFor(get("/pensjonsakrs").willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("psak/psak-hentBrukerForSak-happy.json")));

		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.name();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);

		assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
	}

	@Test
	public void hentBrukerForSakFunctionalErrorEmptyResponse() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_psak-happy.json")));

		stubFor(get("/pensjonsakrs").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("psak/psak-hentBrukerForSak-emptyResponse.json")));

		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.name();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);

		assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
	}

	@Test
	public void hentBrukerForSakFunctionalErrorUnauthorized() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_psak-happy.json")));

		stubFor(get("/pensjonsakrs").willReturn(aResponse().withStatus(HttpStatus.UNAUTHORIZED.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("psak/psak-hentBrukerForSak-happy.json")));

		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.name();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);

		assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
	}

	@Test
	public void hentDokumentNotFound() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND
				.value())));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_gsak-happy.json")));

		stubFor(get("/gsak/10672720").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentsak/hentsakbysaksid-happy.json")));

		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.toString();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);

		assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	public void hentDokumentDecodeFail() {

		byte[] decodeFailProvokerFile = "whitespace breaks base64 decode".getBytes();

		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(decodeFailProvokerFile)));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_gsak-happy.json")));

		stubFor(get("/gsak/10672720").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentsak/hentsakbysaksid-happy.json")));


		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.toString();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
	}

	@Test
	public void hentDokumentAbacDeny() {

		abacDeny();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_gsak-happy.json")));

		stubFor(get("/gsak/10672720").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentsak/hentsakbysaksid-happy.json")));

		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.toString();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);

		assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
	}

	@Test
	public void hentDokumentJoarkTechnicalFail() {

		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR
				.value())));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_gsak-happy.json")));

		stubFor(get("/gsak/10672720").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentsak/hentsakbysaksid-happy.json")));

		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.toString();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
	}

	@Test
	public void hentDokumentHentSakBySakIdTechnicalFailOnTilgangBruker() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_gsak-happy.json")));

		stubFor(get("/gsak/10672720").willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.toString();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);

		assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
	}

	@Test
	public void hentDokumentHentSakBySakIdTechnicalFailOnTilgangSak() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpostTemaBid_gsak-happy.json")));

		stubFor(get("/gsak/55555555")
				.inScenario(SCENARIO_HENTSAK)
				.whenScenarioStateIs(Scenario.STARTED)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentsak/hentsakbysaksidTemaBid-happy.json"))
				.willSetStateTo(STATE_TILGANG_SAK));

		stubFor(get("/gsak/55555555")
				.inScenario(SCENARIO_HENTSAK)
				.whenScenarioStateIs(STATE_TILGANG_SAK)
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.toString();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);

		assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
	}

	@Test
	public void hentDokumentHentSakBySakIdFunctionalFail() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_gsak-happy.json")));

		stubFor(get("/gsak/10672720").willReturn(aResponse().withStatus(HttpStatus.BAD_REQUEST.value())));

		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.toString();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);

		assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
	}

	@Test
	public void hentDokumentHentTilgangJournalPostTechnicalFail() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		stubFor(get("/gsak/10672720").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentsak/hentsakbysaksid-happy.json")));

		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.toString();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);

		assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
	}

	@Test
	public void hentDokumentHentTilgangJournalPostTechnicalFunctionalFailNotFound() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.NOT_FOUND.value())));

		stubFor(get("/gsak/10672720").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentsak/hentsakbysaksid-happy.json")));

		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.toString();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);

		assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
	}

	@Test
	public void hentDokumentHentTilgangJournalPostTechnicalFunctionalFailBadRequest() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.BAD_REQUEST.value())));

		stubFor(get("/gsak/10672720").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentsak/hentsakbysaksid-happy.json")));

		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.toString();
		ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);

		assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
	}
}
