package no.nav.saf.endpoints.hentDokument;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

import com.github.tomakehurst.wiremock.client.BasicCredentials;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.endpoints.AbstractItest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Base64;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
class HentDokumentIT extends AbstractItest {

	private static String DOKUMENT_ID = "123";
	private static String JOURNALPOST_ID = "123";
	private static VariantFormatCode VARIANTFORMAT = VariantFormatCode.ARKIV;
	private static VariantFormatCode SLADDET_VARIANTFORMAT = VariantFormatCode.SLADDET;
	private static byte[] TEST_FILE_BYTES = "TestThis".getBytes();

	@Test
	void hentGsakDokumentHappyPath() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_gsak-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);

		verify(getRequestedFor(urlEqualTo("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)).withBasicAuth(new BasicCredentials("srvsaf", "srvsafpw")));
	}

	@Test
	void hentGsakDokumentHappyPathBrukerOrganisasjon() {
		abacPermitExceptPep1();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_gsak-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)).withBasicAuth(new BasicCredentials("srvsaf", "srvsafpw")));
	}


	@Test
	void hentPsakDokumentHappyPath() {
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
		stubFor(post("/pensjonsakv1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-hentdokument-happy.xml")));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)).withBasicAuth(new BasicCredentials("srvsaf", "srvsafpw")));
		verify(getRequestedFor(urlEqualTo("/pensjonsakrs")).withBasicAuth(new BasicCredentials("srvsaf", "srvsafpw")));
		verify(getRequestedFor(urlEqualTo("/pensjonsakrs")).withHeader("sakId", equalTo("10672720")));
	}

	@Test
	void hentMidlertidigDokumentHappyPath() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_midlertidig-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT))
				.withBasicAuth(new BasicCredentials("srvsaf", "srvsafpw")));
	}

	@Test
	void hentGsakDokumentSladdet() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + SLADDET_VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + SLADDET_VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_gsak-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokumentSladdetVariant();

		assertOkSladdetResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + SLADDET_VARIANTFORMAT)).withBasicAuth(new BasicCredentials("srvsaf", "srvsafpw")));
	}

	@Test
	void hentBrukerForSakTechnicalError() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_psak-happy.json")));

		stubFor(get("/pensjonsakrs").willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("psak/psak-hentBrukerForSak-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void hentBrukerForSakFunctionalErrorEmptyResponse() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_psak-happy.json")));

		stubFor(get("/pensjonsakrs").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("psak/psak-hentBrukerForSak-emptyResponse.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void hentBrukerForSakFunctionalErrorUnauthorized() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_psak-happy.json")));

		stubFor(get("/pensjonsakrs").willReturn(aResponse().withStatus(HttpStatus.UNAUTHORIZED.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("psak/psak-hentBrukerForSak-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void hentDokumentNotFound() {
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

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void hentDokumentDecodeFail() {

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

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
	}


	@Test
	void hentDokumentJoarkTechnicalFail() {

		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR
				.value())));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_gsak-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
	}

	@Test
	void hentDokumentHentSakBySakId() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_gsak-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	void hentDokumentHentSakBySakIdNoTechinalErrorForGsak() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpostTemaBid_gsak-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	void hentDokumentHentSakBySakIdFunctionalFail() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_gsak-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
	}

	@Test
	void hentDokumentHentTilgangJournalPostTechnicalFail() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void hentDokumentHentTilgangJournalPostTechnicalFunctionalFailNotFound() {
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

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void hentDokumentHentTilgangJournalPostTechnicalFunctionalFailBadRequest() {
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

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep1g() {
		abacDenyPep1g();
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

		ResponseEntity<String> responseEntity = callHentDokument();

		verifyabacDenyPep1gAndHttpStatusCode(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep2() {
		abacDenyPep2();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/bidrag/765432").willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpostTemaBid_gsak-happy.json")));

		stubFor(get("/gsak/55555555").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentsak/hentsakbysaksidTemaBid-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		verifyabacDenyPep2AndHttpStatusCode(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep2d() {
		abacDenyPep2d();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/bidrag/765432").willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpostTemaBid_gsak-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		verifyabacDenyPep2dAndHttpStatusCode(false, HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep3() {
		abacDenyPep3();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/bidrag/765432").willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpostTemaBid_gsak-happy.json")));

		stubFor(get("/gsak/55555555").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentsak/hentsakbysaksidTemaBid-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		verifyabacDenyPep3AndHttpStatusCode(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep4() {
		abacDenyPep4();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/bidrag/765432").willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpostTemaBidWithSkjerming_gsak-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		verifyabacDenyPep4AndHttpStatusCode(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
	}


	@Test
	void shouldGetUnauthorizedFromPep5() {
		abacDenyPep5();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/bidrag/765432").willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpostTemaBidWithSkjerming_gsak-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		verifyabacDenyPep5AndHttpStatusCode(false, HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep6d() {
		abacDenyPep6d();
		stubFor(get("/hentjournalsakinfo/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse().withStatus(HttpStatus.OK
				.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE)
				.withBody(Base64.getEncoder().encode(TEST_FILE_BYTES))));

		stubFor(get("/bidrag/765432").willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT).willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("hentjournalsakinfo/henttilgangjournalpostTemaBidWithSkjerming_gsak-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		verifyabacDenyPep6dAndHttpStatusCode(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
	}

	private void assertOkArkivResponse(ResponseEntity<String> responseEntity) {
		assertEquals(DOKUMENT_ID + "_" + VARIANTFORMAT + ".pdf", responseEntity.getHeaders().getContentDisposition().getFilename());
	}

	private void assertOkSladdetResponse(ResponseEntity<String> responseEntity) {
		assertEquals(DOKUMENT_ID + "_" + SLADDET_VARIANTFORMAT + ".pdf", responseEntity.getHeaders().getContentDisposition().getFilename());
		assertOkResponse(responseEntity);
	}

	private void assertOkResponse(ResponseEntity<String> responseEntity) {
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(MediaType.APPLICATION_PDF, responseEntity.getHeaders().getContentType());
		assertEquals("inline", responseEntity.getHeaders().getContentDisposition().getType());
		assertEquals(new String(TEST_FILE_BYTES), responseEntity.getBody());
	}

	private ResponseEntity<String> callHentDokument() {
		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT.toString();
		return this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);
	}


	private ResponseEntity<String> callHentDokumentSladdetVariant() {
		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + SLADDET_VARIANTFORMAT.toString();
		return this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);
	}
}
