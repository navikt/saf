package no.nav.saf.endpoints.hentDokument;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.endpoints.AbstractItest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode.ARKIV;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode.ORIGINAL;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode.SLADDET;
import static no.nav.saf.hentdokument.HentDokumentAntiCorruptionLayer.HENTDOKUMENT_TILGANG_FIELDS;
import static no.nav.saf.tilgangskontroll.pep.DenyReasonFactory.PEP1G_DENY_REASON;
import static no.nav.saf.tilgangskontroll.pep.DenyReasonFactory.PEP2_DENY_REASON;
import static no.nav.saf.tilgangskontroll.pep.DenyReasonFactory.PEP3_DENY_REASON;
import static no.nav.saf.tilgangskontroll.pep.DenyReasonFactory.PEP4_DENY_REASON;
import static no.nav.saf.tilgangskontroll.pep.DenyReasonFactory.PEP5_DENY_REASON;
import static no.nav.saf.tilgangskontroll.pep.DenyReasonFactory.PEP6D_DENY_REASON;
import static no.nav.saf.tilgangskontroll.pep.DenyReasonFactory.PEP7D_DENY_REASON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.http.MediaType.TEXT_XML;
import static org.springframework.http.MediaType.TEXT_XML_VALUE;

class HentDokumentIT extends AbstractItest {

	private static final String JOURNALPOST_ID = "400000000";
	private static final String DOKUMENT_ID = "500000000";
	private static final VariantFormatCode VARIANTFORMAT = ARKIV;
	private static final VariantFormatCode SLADDET_VARIANTFORMAT = SLADDET;
	private static final byte[] TEST_FILE_BYTES = "TestThis".getBytes();

	@BeforeEach
	public void setup() {
		setupHappyPathRestSTS();
		setupHappyPathAzureToken();
	}

	@Test
	void shouldHentDokumentWhenHappy() {
		abacPermit();
		stubHappyHentDokument();
		stubDokarkivJournalpost("journalpost-dokumentinfo-gsak-happy.json");

		Logger logger = (Logger) LoggerFactory.getLogger("hentdokument_sporbarhetslogg");
		ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
		listAppender.start();
		logger.addAppender(listAppender);

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)));

		List<String> auditLog = listAppender.list.stream().map(ILoggingEvent::getMessage).toList();
		assertThat(auditLog).hasSize(1);

		String cefLogLine = auditLog.getFirst();
		assertThat(cefLogLine).startsWith("CEF:0|joark|saf_hentdokument|1.0|audit:access|Saksbehandler hentet dokument som gjelder bruker|INFO|");
		assertThat(cefLogLine).contains(
				"duid=12345678910",
				"suid=" + NAV_IDENT_SAKSBEHANDLER,
				"cs3=ARKIV",
				"cs3Label=variantformat",
				"cs5=Journalposttittel – med mellomrom? It's more likely than you think",
				"cs5Label=tittel",
				"cs6=HJE",
				"cs6Label=tema",
				"flexString1=" + JOURNALPOST_ID,
				"flexString1Label=journalpostId",
				"flexString2=" + DOKUMENT_ID,
				"flexString2Label=dokumentInfoId",
				"act=hentdokument_saksbehandler",
				"sproc=",
				"end=");
	}

	@Test
	void shouldHentXmlOriginalWhenHappy() {
		abacPermit();
		stubHappyHentDokumentXml();
		stubDokarkivJournalpost("journalpost-dokumentinfo-gsak-happy.json");

		ResponseEntity<String> responseEntity = callHentDokument(ORIGINAL);

		assertOkXmlOriginalResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + ORIGINAL)));
	}

	@Test
	void shouldHentJsonOriginalWhenHappy() {
		abacPermit();
		stubHappyHentDokumentJson();
		stubDokarkivJournalpost("journalpost-dokumentinfo-gsak-happy.json");

		ResponseEntity<String> responseEntity = callHentDokument(ORIGINAL);

		assertOkJsonResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + ORIGINAL)));
	}

	@Test
	void shouldHentDokumentHappyPathBrukerOrganisasjon() {
		abacPermit();
		stubNavHrOrganisasjonNei(ORG_NR);
		stubHappyHentDokument();
		stubDokarkivJournalpost("journalpost-dokumentinfo-gsak-org-happy.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)));
	}

	@Test
	void shouldNotHentDokumentWhenBrukerErOrganisasjonAndNotEgenAnsatt() {
		abacPermit();
		stubNavHrOrganisasjonJa(ORG_NR);
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfNotEgenAnsatt(MS_ID_SAKSBEHANDLER);
		stubDokarkivJournalpost("journalpost-dokumentinfo-gsak-org-happy.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getBody()).contains("Journalpost/dokument er knyttet til organisasjon underlagt NAV og det krever egen ansatt behandling for oppslag på denne.");
	}

	@Test
	void shouldHentDokumentWhenBrukerErOrganisasjonAndIsEgenAnsattBehandler() {
		abacPermit();
		stubNavHrOrganisasjonJa(ORG_NR);
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfEgenAnsatt(MS_ID_SAKSBEHANDLER);
		stubHappyHentDokument();
		stubDokarkivJournalpost("journalpost-dokumentinfo-gsak-org-happy.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)));
	}

	@Test
	void shouldHentDokumentWhenBrukerErOrganisasjonAndIsEgenAnsattBehandlerAndOrgnrWhitespace() {
		abacPermit();
		stubNavHrOrganisasjonJa(ORG_NR);
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfEgenAnsatt(MS_ID_SAKSBEHANDLER);
		stubHappyHentDokument();
		stubDokarkivJournalpost("journalpost-dokumentinfo-gsak-org-whitespace.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)));
	}

	@Test
	void shouldHentDokumentWhenSakPsakHappy() {
		abacPermit();
		stubHappyHentDokument();
		stubDokarkivJournalpost("journalpost-dokumentinfo-pensjon-happy.json");
		stubPensjonBrukerForSak();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)));
		verify(getRequestedFor(urlEqualTo(PENSJON_API_PIP_HENT_BRUKER_OG_ENHETSTILGANGER_FOR_SAK_V1_URL)).withHeader(AUTHORIZATION, equalTo("Bearer AzureAccessToken")));
		verify(getRequestedFor(urlEqualTo(PENSJON_API_PIP_HENT_BRUKER_OG_ENHETSTILGANGER_FOR_SAK_V1_URL)).withHeader("sakId", equalTo("240000000")));
	}

	@Test
	void shouldHentDokumentWhenMidlertidigHappy() {
		abacPermit();
		stubHappyHentDokument();
		stubDokarkivJournalpost("journalpost-dokumentinfo-gsak-midlertidig-happy.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)));
	}

	@Test
	void shouldHentDokumentWhenMidlertidigAndBrukerTypeNull() {
		abacPermit();
		stubHappyHentDokument();
		stubDokarkivJournalpost("journalpost-dokumentinfo-gsak-midlertidig-bruker-empty.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)));
	}

	@Test
	void shouldHentDokumentWhenMidlertidigSaksrelasjonEmpty() {
		abacPermit();
		stubHappyHentDokument();
		stubDokarkivJournalpost("journalpost-dokumentinfo-gsak-midlertidig-saksrelasjon-empty.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)));
	}

	@Test
	void shouldHentDokumentWhenSladdet() {
		abacPermit();
		stubFor(get("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + SLADDET_VARIANTFORMAT)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_PDF_VALUE)
						.withBody(TEST_FILE_BYTES)));
		stubDokarkivJournalpost("journalpost-dokumentinfo-gsak-sladdet-happy.json");

		ResponseEntity<String> responseEntity = callHentDokumentSladdetVariant();

		assertOkSladdetResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + SLADDET_VARIANTFORMAT)));
	}

	@Test
	void shouldNotReturnDokumentWhenPensjonHentBrukerForSakTechnicalError() {
		abacPermit();
		stubDokarkivJournalpost("journalpost-dokumentinfo-pensjon-happy.json");
		stubFor(get(PENSJON_API_PIP_HENT_BRUKER_OG_ENHETSTILGANGER_FOR_SAK_V1_URL)
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void shouldNotReturnDokumentWhenPensjonHentBrukerForSakFunctionalErrorEmptyResponse() {
		abacPermit();
		stubDokarkivJournalpost("journalpost-dokumentinfo-pensjon-happy.json");
		stubFor(get(PENSJON_API_PIP_HENT_BRUKER_OG_ENHETSTILGANGER_FOR_SAK_V1_URL)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("psak/psak-hentBrukerForSak-emptyResponse.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void shouldNotReturnDokumentWhenPensjonHentBrukerForSakFunctionalErrorUnauthorized() {
		abacPermit();
		stubDokarkivJournalpost("journalpost-dokumentinfo-pensjon-happy.json");
		stubFor(get(PENSJON_API_PIP_HENT_BRUKER_OG_ENHETSTILGANGER_FOR_SAK_V1_URL)
				.willReturn(aResponse().withStatus(HttpStatus.UNAUTHORIZED.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void shouldReturnNotFoundWhenDokumentNotFound() {
		abacPermit();
		stubFor(get("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(NOT_FOUND.value())));
		stubDokarkivJournalpost("journalpost-dokumentinfo-gsak-happy.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void shouldReturnInternalServerErrorWhenHentDokumentJoarkTechnicalFail() {
		abacPermit();
		stubFor(get("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())));
		stubDokarkivJournalpost("journalpost-dokumentinfo-gsak-happy.json");

		ResponseEntity<String> responseEntity = callHentDokument();
		assertEquals(INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
	}

	@Test
	void shouldReturnInternalServerErrorWhenHentJournalpostTechnicalFail() {
		abacPermit();
		stubDokarkivJournalpost(INTERNAL_SERVER_ERROR);

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
	}

	@Test
	void shouldReturnNotFoundWhenOriginalVariantDoesNotExist() {
		abacPermit();
		stubDokarkivJournalpost("journalpost-dokumentinfo-gsak-dokumentvariant-notmatched.json");

		ResponseEntity<String> responseEntity = callHentDokument(ORIGINAL);

		assertEquals(NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void shouldReturnNotFoundWhenJournalpostMetadataNotFound() {
		abacPermit();
		stubDokarkivJournalpost(NOT_FOUND);

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void shouldReturnInternalServerErrorWhenJournalpostMetadataBadRequest() {
		abacPermit();
		stubDokarkivJournalpost(BAD_REQUEST);

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetForbiddenFromPep1g() {
		abacDenyPep1g();
		stubDokarkivJournalpost("journalpost-dokumentinfo-gsak-happy.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		verifyabacDenyPep1gAndHttpStatusCode(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains(PEP1G_DENY_REASON);
	}

	@Test
	void shouldGetForbiddenFromPep2() {
		abacDenyPep2();
		stubDokarkivJournalpost("journalpost-dokumentinfo-gsak-tema-far.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		verifyabacDenyPep2AndHttpStatusCode(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains(PEP2_DENY_REASON);
	}

	@Test
	@DisplayName("Skal ikke hente farskap dokument hvis journalpost ikke har sakstilknytning eller bruker")
	void shouldGetForbiddenFromPep2WhenMidlertidigJournalpost() {
		abacDenyPep2MidlertidigJournalpost();
		stubDokarkivJournalpost("journalpost-dokumentinfo-gsak-midlertidig-ingen-bruker-tema-far.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains(PEP2_DENY_REASON);
	}

	@Test
	void shouldGetForbiddenFromPep2d() {
		abacDenyPep2d();
		stubHappyBisysSak();
		stubDokarkivJournalpost("journalpost-dokumentinfo-gsak-tema-bid.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains("Saksbehandler har ikke tilgang til tema ressursen tilhører eller geografisk område");
	}

	@Test
	void shouldGetForbiddenFromPep3() {
		abacDenyPep3SkipPep2dAndPep2();
		stubHappyBisysSak();
		stubDokarkivJournalpost("journalpost-dokumentinfo-gsak-tema-bid.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains(PEP3_DENY_REASON);
	}

	@Test
	void shouldGetForbiddenFromPep4WhenJournalstatusUtgaar() {
		abacDenyPep4SkipPep2OrPep3();
		stubHappyBisysSak();
		stubDokarkivJournalpost("journalpost-dokumentinfo-gsak-tema-bid-utgaar.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains(PEP4_DENY_REASON);
	}

	@Test
	void shouldGetForbiddenFromPep4WhenJournalpostSkjermet() {
		abacDenyPep4SkipPep2OrPep3();
		stubHappyBisysSak();
		stubDokarkivJournalpost("journalpost-dokumentinfo-gsak-tema-bid-journalpost-skjerming.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains(PEP4_DENY_REASON);
	}

	@Test
	void shouldGetForbiddenFromPep5WhenDokumentinfoSkjermet() {
		abacDenyPep5SkipPep4();
		stubHappyBisysSak();
		stubDokarkivJournalpost("journalpost-dokumentinfo-gsak-tema-bid-dokumentinfo-skjerming.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains(PEP5_DENY_REASON);
	}

	@Test
	void shouldGetForbiddenFromPep6d() {
		abacDenyPep6dSkipPep2Pep4Pep5();
		stubHappyHentDokument();
		stubHappyBisysSak();
		stubDokarkivJournalpost("journalpost-dokumentinfo-gsak-tema-bid-fildetaljer-skjerming.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains(PEP6D_DENY_REASON);
	}

	@Test
	void shouldGetForbiddenFromPep7dWhenTemaForeldrepenger() {
		abacDenyPep7dSkipPep2Pep3Pep4Pep5Pep6d();
		stubDokarkivJournalpost("journalpost-dokumentinfo-gsak-tema-for.json");
		stubFor(get("/fpsak?saksnummer=FOR2000")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("fpsak/happy-response.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains(PEP7D_DENY_REASON);
	}

	@Test
	void shouldGetForbiddenFromPep7dWhenK9TemaFri() {
		abacDenyPep7dSkipPep2Pep3Pep4Pep5Pep6d();
		stubDokarkivJournalpost("journalpost-dokumentinfo-gsak-tema-fri.json");
		stubFor(get("/k9sak?saksnummer=K92000")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("k9/happy-response.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains(PEP7D_DENY_REASON);
	}

	@Test
	void shouldGetForbiddenFromPep7ForK9TemaOms() {
		abacDenyPep7dSkipPep2Pep3Pep4Pep5Pep6d();
		stubDokarkivJournalpost("journalpost-dokumentinfo-gsak-tema-oms.json");
		stubFor(get("/k9sak?saksnummer=K92000")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("k9/happy-response.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains(PEP7D_DENY_REASON);
	}

	private static void stubHappyHentDokument() {
		stubFor(get("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_PDF_VALUE)
						.withBody(TEST_FILE_BYTES)));
	}

	private static void stubHappyHentDokumentXml() {
		stubFor(get("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + ORIGINAL)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, TEXT_XML_VALUE)
						.withBody(TEST_FILE_BYTES)));
	}

	private static void stubHappyHentDokumentJson() {
		stubFor(get("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + ORIGINAL)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBody(TEST_FILE_BYTES)));
	}

	private static void stubDokarkivJournalpost(String fil) {
		stubFor(get("/dokarkiv/journalpost/journalpostId/" + JOURNALPOST_ID + "/dokumentInfoId/" + DOKUMENT_ID + "?fields=" + String.join(",", HENTDOKUMENT_TILGANG_FIELDS))
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/journalpostdokumentinfo/" + fil)));
	}

	private static void stubDokarkivJournalpost(HttpStatus httpStatus) {
		stubFor(get("/dokarkiv/journalpost/journalpostId/" + JOURNALPOST_ID + "/dokumentInfoId/" + DOKUMENT_ID + "?fields=" + String.join(",", HENTDOKUMENT_TILGANG_FIELDS))
				.willReturn(aResponse()
						.withStatus(httpStatus.value())));
	}

	private static void stubHappyBisysSak() {
		stubFor(get("/bidrag/BISYS2000")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("bidrag/bidragsak-happy.json")));
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

	private void assertOkXmlOriginalResponse(ResponseEntity<String> responseEntity) {
		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals(TEXT_XML, responseEntity.getHeaders().getContentType());
		assertEquals("inline", responseEntity.getHeaders().getContentDisposition().getType());
		assertEquals(new String(TEST_FILE_BYTES), responseEntity.getBody());
		assertEquals(DOKUMENT_ID + "_" + ORIGINAL + ".xml", responseEntity.getHeaders().getContentDisposition().getFilename());
	}

	private void assertOkJsonResponse(ResponseEntity<String> responseEntity) {
		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals(APPLICATION_JSON, responseEntity.getHeaders().getContentType());
		assertEquals("inline", responseEntity.getHeaders().getContentDisposition().getType());
		assertEquals(new String(TEST_FILE_BYTES), responseEntity.getBody());
		assertEquals(DOKUMENT_ID + "_" + ORIGINAL + ".json", responseEntity.getHeaders().getContentDisposition().getFilename());
	}

	private ResponseEntity<String> callHentDokument() {
		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + VARIANTFORMAT;
		return this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);
	}

	private ResponseEntity<String> callHentDokument(VariantFormatCode variantFormat) {
		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + variantFormat;
		return this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);
	}

	private ResponseEntity<String> callHentDokumentSladdetVariant() {
		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + SLADDET_VARIANTFORMAT;
		return this.restTemplate.exchange(uri, HttpMethod.GET, createHttpEntity(), String.class);
	}
}
