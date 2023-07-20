package no.nav.saf.endpoints.hentDokument;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.endpoints.AbstractItest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode.ARKIV;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode.SLADDET;
import static no.nav.saf.tilgangskontroll.pep.DenyReasonFactory.PEP2_DENY_REASON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

class HentDokumentIT extends AbstractItest {

	private static final String DOKUMENT_ID = "123";
	private static final String JOURNALPOST_ID = "123";
	private static final String SAK_ID = "10672720";
	private static final VariantFormatCode VARIANTFORMAT = ARKIV;
	private static final VariantFormatCode SLADDET_VARIANTFORMAT = SLADDET;
	private static final byte[] TEST_FILE_BYTES = "TestThis".getBytes();

	@BeforeEach
	public void setup() {
		setupHappyPathRestSTS();
		setupHappyPathAzureToken();
	}

	@Test
	void hentGsakDokumentHappyPath() {
		abacPermit();
		stubHappyHentDokument();
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_gsak-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)));
	}

	@Test
	void hentGsakDokumentHappyPathBrukerOrganisasjon() {
		abacPermit();
		stubHappyHentDokument();
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_gsak-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)));
	}


	@Test
	void hentPsakDokumentHappyPath() {
		abacPermit();
		stubHappyHentDokument();
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_psak-happy.json")));
		stubFor(get("/pensjonsakrs")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("psak/psak-hentBrukerForSak-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)));
		verify(getRequestedFor(urlEqualTo("/pensjonsakrs")).withHeader(AUTHORIZATION, equalTo("Bearer AzureAccessToken")));
		verify(getRequestedFor(urlEqualTo("/pensjonsakrs")).withHeader("sakId", equalTo("10672720")));
	}

	@Test
	void hentMidlertidigDokumentHappyPath() {
		abacPermit();
		stubHappyHentDokument();
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_midlertidig-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)));
	}

	@Test
	void hentGsakDokumentSladdet() {
		abacPermit();
		stubFor(get("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + SLADDET_VARIANTFORMAT)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_PDF_VALUE)
						.withBody(TEST_FILE_BYTES)));
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + SLADDET_VARIANTFORMAT)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_gsak-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokumentSladdetVariant();

		assertOkSladdetResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + SLADDET_VARIANTFORMAT)));
	}

	@Test
	void hentBrukerForSakTechnicalError() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_psak-happy.json")));
		stubFor(get("/pensjonsakrs")
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("psak/psak-hentBrukerForSak-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();
		assertEquals(NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void hentBrukerForSakFunctionalErrorEmptyResponse() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_psak-happy.json")));
		stubFor(get("/pensjonsakrs")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("psak/psak-hentBrukerForSak-emptyResponse.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void hentBrukerForSakFunctionalErrorUnauthorized() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_psak-happy.json")));
		stubFor(get("/pensjonsakrs")
				.willReturn(aResponse().withStatus(HttpStatus.UNAUTHORIZED.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("psak/psak-hentBrukerForSak-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void hentDokumentNotFound() {
		abacPermit();
		stubFor(get("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(NOT_FOUND.value())));
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_gsak-happy.json")));
		stubFor(get("/gsak/10672720")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentsak/hentsakbysaksid-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();
		assertEquals(NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void hentDokumentJoarkTechnicalFail() {
		abacPermit();
		stubFor(get("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())));
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_gsak-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();
		assertEquals(INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
	}

	@Test
	void hentDokumentHentSakBySakId() {
		abacPermit();
		stubHappyHentDokument();
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_gsak-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();
		assertEquals(OK, responseEntity.getStatusCode());
	}

	@Test
	void hentDokumentHentSakBySakIdNoTechinalErrorForGsak() {
		abacPermit();
		stubHappyHentDokument();
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/henttilgangjournalpostTemaBid_gsak-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();
		assertEquals(OK, responseEntity.getStatusCode());
	}

	@Test
	void hentDokumentHentSakBySakIdFunctionalFail() {
		abacPermit();
		stubHappyHentDokument();
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_gsak-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();
		assertOkArkivResponse(responseEntity);
	}

	@Test
	void hentDokumentHentTilgangJournalPostTechnicalFail() {
		abacPermit();
		stubHappyHentDokument();
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse()
						.withStatus(INTERNAL_SERVER_ERROR.value())));

		ResponseEntity<String> responseEntity = callHentDokument();
		assertEquals(INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
	}

	@Test
	void hentDokumentHentTilgangJournalPostTechnicalFunctionalFailNotFound() {
		abacPermit();
		stubHappyHentDokument();
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse()
						.withStatus(NOT_FOUND.value())));
		stubFor(get("/gsak/10672720")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentsak/hentsakbysaksid-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();
		assertEquals(NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void hentDokumentHentTilgangJournalPostTechnicalFunctionalFailBadRequest() {
		abacPermit();
		stubHappyHentDokument();
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse()
						.withStatus(HttpStatus.BAD_REQUEST.value())));
		stubFor(get("/gsak/10672720")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentsak/hentsakbysaksid-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();
		assertEquals(BAD_REQUEST, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep1g() {
		abacDenyPep1g();
		stubHappyHentDokument();
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_gsak-happy.json")));
		stubFor(get("/gsak/10672720")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentsak/hentsakbysaksid-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();
		verifyabacDenyPep1gAndHttpStatusCode(FORBIDDEN, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep2() {
		abacDenyPep2();
		stubHappyHentDokument();
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/henttilgangjournalpostTemaFar_gsak-happy.json")));
		stubFor(get("/gsak/55555555")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentsak/hentsakbysaksidTemaFar-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();
		verifyabacDenyPep2AndHttpStatusCode(FORBIDDEN, responseEntity.getStatusCode());
	}

	@Test
	@DisplayName("Skal ikke hente farskap dokument hvis journalpost ikke har sakstilknytning eller bruker")
	void shouldGetUnauthorizedFromPep2WhenMidlertidigJournalpost() {
		abacDenyPep2MidlertidigJournalpost();
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_far_midlertidig.json")));

		ResponseEntity<String> responseEntity = callHentDokument();
		assertEquals(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains(PEP2_DENY_REASON);
	}

	@Test
	void shouldGetUnauthorizedFromPep2d() {
		abacDenyPep2d();
		stubHappyHentDokument();
		stubFor(get("/bidrag/765432")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("bidrag/bidragsak-happy.json")));
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/henttilgangjournalpostTemaBid_gsak-happy.json")));
		stubFor(get("/gsak/10672720")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentsak/hentsakbysaksid-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();
		verifyabacDenyPep2dAndHttpStatusCode(false, FORBIDDEN, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep3() {
		abacDenyPep3SkipPep2dAndPep2();
		stubHappyHentDokument();
		stubFor(get("/bidrag/765432")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("bidrag/bidragsak-happy.json")));
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/henttilgangjournalpostTemaBid_gsak-happy.json")));
		stubFor(get("/gsak/55555555")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentsak/hentsakbysaksidTemaBid-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();
		verifyabacDenyPep3SkipPep2AndPep2dAndHttpStatusCode(FORBIDDEN, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep4() {
		abacDenyPep4SkipPep2OrPep3();
		stubHappyHentDokument();
		stubFor(get("/bidrag/765432")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("bidrag/bidragsak-happy.json")));
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/henttilgangjournalpostTemaBidWithSkjerming_gsak-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();
		verifyabacDenyPep4SkipPep2OrPep3AndHttpStatusCode(FORBIDDEN, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep5() {
		abacDenyPep5SkipPep2OrPep3();
		stubHappyHentDokument();
		stubFor(get("/bidrag/765432")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("bidrag/bidragsak-happy.json")));
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/henttilgangjournalpostTemaBidWithSkjerming_gsak-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();
		verifyabacDenyPep5SkipPep2OrPep3AndHttpStatusCode(false, FORBIDDEN, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep6d() {
		abacDenyPep6dSkipPep3OrPep2();
		stubHappyHentDokument();
		stubFor(get("/bidrag/765432")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("bidrag/bidragsak-happy.json")));
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/henttilgangjournalpostTemaBidWithSkjerming_gsak-happy.json")));

		ResponseEntity<String> responseEntity = callHentDokument();
		verifyabacDenyPep6dSkipPep2AndHttpStatusCode(FORBIDDEN, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep7ForFp() {
		abacDenyPep7dSkipPep2Pep3Pep4Pep5Pep6d();
		stubHappyHentDokument();
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/henttilgangjournalpost_gsak-happy.json")));
		stubFor(get("/fpsak?saksnummer=" + SAK_ID)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("fpsak/happy-response.json")));

		ResponseEntity<String> responseEntity = callHentDokument();
		verifyabacDenyPep7dSkipPep2Pep3Pep4Pep5Pep6dAndHttpStatusCode(FORBIDDEN, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep7ForK9TemaFri() {
		abacDenyPep7dSkipPep2Pep3Pep4Pep5Pep6d();
		stubHappyHentDokument();
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/henttilgangjournalpostTemaFri_gsak-happy.json")));
		stubFor(get("/k9sak?saksnummer=" + SAK_ID)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("k9/happy-response.json")));

		ResponseEntity<String> responseEntity = callHentDokument();
		verifyabacDenyPep7dSkipPep2Pep3Pep4Pep5Pep6dAndHttpStatusCode(FORBIDDEN, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep7ForK9TemaOms() {
		abacDenyPep7dSkipPep2Pep3Pep4Pep5Pep6d();
		stubHappyHentDokument();
		stubFor(get("/hentjournalsakinfo/henttilgangjournalpost/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/henttilgangjournalpostTemaOms_gsak-happy.json")));
		stubFor(get("/k9sak?saksnummer=" + SAK_ID)
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("k9/happy-response.json")));

		ResponseEntity<String> responseEntity = callHentDokument();
		verifyabacDenyPep7dSkipPep2Pep3Pep4Pep5Pep6dAndHttpStatusCode(FORBIDDEN, responseEntity.getStatusCode());
	}

	private static void stubHappyHentDokument() {
		stubFor(get("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_PDF_VALUE)
						.withBody(TEST_FILE_BYTES)));
	}

	private void assertOkArkivResponse(ResponseEntity<String> responseEntity) {
		assertEquals(DOKUMENT_ID + "_" + VARIANTFORMAT + ".pdf", responseEntity.getHeaders().getContentDisposition().getFilename());
	}

	private void assertOkSladdetResponse(ResponseEntity<String> responseEntity) {
		assertEquals(DOKUMENT_ID + "_" + SLADDET_VARIANTFORMAT + ".pdf", responseEntity.getHeaders().getContentDisposition().getFilename());
		assertOkResponse(responseEntity);
	}

	private void assertOkResponse(ResponseEntity<String> responseEntity) {
		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals(APPLICATION_PDF, responseEntity.getHeaders().getContentType());
		assertEquals("inline", responseEntity.getHeaders().getContentDisposition().getType());
		assertEquals(new String(TEST_FILE_BYTES), responseEntity.getBody());
	}

	private ResponseEntity<String> callHentDokument() {
		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT;
		return this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);
	}

	private ResponseEntity<String> callHentDokumentSladdetVariant() {
		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + SLADDET_VARIANTFORMAT;
		return this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);
	}
}
