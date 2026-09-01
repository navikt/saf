package no.nav.saf.endpoints.hentDokument;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.endpoints.AbstractItest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
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
	private static final VariantFormatCode ORIGINAL_VARIANTFORMAT = ORIGINAL;
	private static final byte[] TEST_FILE_BYTES = "TestThis".getBytes();
	private static final String USER_FNR_FROM_T_BRUKER = "12345678910";
	private static final String USER_FNR_FROM_PDL = "11111111111";

	@BeforeEach
	public void setup() {
		setupHappyPathAzureToken();
	}

	@Test
	void shouldHentDokumentWhenHappy() {
		tilgangskontrollPermit();
		stubHappyHentDokument();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-happy.json");
		stubPdl();

		ListAppender<ILoggingEvent> listAppender = initialiseLogAppender();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)));
		verify(postRequestedFor(urlEqualTo("/pdl")));

		List<String> auditLog = listAppender.list.stream().map(ILoggingEvent::getMessage).toList();
		assertThat(auditLog).hasSize(1);

		String cefLogLine = auditLog.getFirst();
		assertThat(cefLogLine).startsWith("CEF:0|joark|saf_hentdokument|1.0|audit:access|Saksbehandler hentet dokument som gjelder bruker|INFO|");
		assertThat(cefLogLine).contains(
				"duid=" + USER_FNR_FROM_PDL,
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
				"shost=dev-itest:isa:gosys",
				"sproc=",
				"end=");
	}

	@Test
	void shouldHentDokumentWhenBrukerIsOrganisationAndHasAktoerId() {
		tilgangskontrollPermit();
		stubHappyHentDokument();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-bruker-organisasjon-og-aktoerid.json");
		stubPdl();

		ListAppender<ILoggingEvent> listAppender = initialiseLogAppender();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)));
		verify(postRequestedFor(urlEqualTo("/pdl")));

		List<String> auditLog = listAppender.list.stream().map(ILoggingEvent::getMessage).toList();
		assertThat(auditLog).hasSize(1);

		String cefLogLine = auditLog.getFirst();
		assertThat(cefLogLine).startsWith("CEF:0|joark|saf_hentdokument|1.0|audit:access|Saksbehandler hentet dokument som gjelder bruker|INFO|");
		assertThat(cefLogLine).contains(
				"duid=" + USER_FNR_FROM_PDL,
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
		tilgangskontrollPermit();
		stubHappyHentDokumentXml();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-happy.json");
		stubPdl();

		ResponseEntity<String> responseEntity = callHentDokument(ORIGINAL);

		assertOkXmlOriginalResponse(responseEntity);
		verify(postRequestedFor(urlEqualTo("/pdl")));
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + ORIGINAL)));
	}

	@Test
	void shouldHentJsonOriginalWhenHappy() {
		tilgangskontrollPermit();
		stubPdl();
		stubHappyHentDokumentJson();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-happy.json");

		ResponseEntity<String> responseEntity = callHentDokument(ORIGINAL);

		verify(postRequestedFor(urlEqualTo("/pdl")));
		assertOkJsonResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + ORIGINAL)));
	}

	@Test
	void shouldHentDokumentHappyPathBrukerOrganisasjon() {
		tilgangskontrollPermit();
		stubNavHrOrganisasjonNei(ORG_NR);
		stubHappyHentDokument();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-org-happy.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)));
	}

	@Test
	void shouldHentDokumentWhenBrukerErOrganisasjonAndIsEgenAnsattBehandler() {
		tilgangskontrollPermit();
		stubNavHrOrganisasjonJa(ORG_NR);
		stubHappyHentDokument();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-org-happy.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)));
		verifyMsGraphMemberOfSeveralGroupsCalled(MS_ID_SAKSBEHANDLER, 1);
	}

	@Test
	void shouldHentDokumentWhenBrukerErOrganisasjonAndIsEgenAnsattBehandlerAndOrgnrWhitespace() {
		tilgangskontrollPermit();
		stubNavHrOrganisasjonJa(ORG_NR);
		stubHappyHentDokument();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-org-whitespace.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)));
		verifyMsGraphMemberOfSeveralGroupsCalled(MS_ID_SAKSBEHANDLER, 1);
	}

	@Test
	void shouldHentDokumentWhenSakPsakHappy() {
		tilgangskontrollPermit();
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
		tilgangskontrollPermit();
		stubHappyHentDokument();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-midlertidig-happy.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)));
	}

	@Test
	void shouldHentDokumentWhenMidlertidigAndBrukerTypeNull() {
		tilgangskontrollPermit();
		stubHappyHentDokument();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-midlertidig-bruker-empty.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)));
	}

	@Test
	void shouldHentDokumentWhenMidlertidigSaksrelasjonEmpty() {
		tilgangskontrollPermit();
		stubHappyHentDokument();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-midlertidig-saksrelasjon-empty.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)));
	}

	@Test
	void shouldHentDokumentWhenSladdet() {
		tilgangskontrollPermit();
		stubPdl();
		stubHappyHentDokument(SLADDET_VARIANTFORMAT);
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-sladdet-happy.json");

		ResponseEntity<String> responseEntity = callHentDokumentSladdetVariant();

		verify(postRequestedFor(urlEqualTo("/pdl")));
		assertOkSladdetResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + SLADDET_VARIANTFORMAT)));
	}

	@Test
	void shouldHentSladdetDokumentWhenVariantFormatIsNotSpecified() {
		tilgangskontrollPermit();
		stubPdl();
		stubHappyHentDokument(SLADDET_VARIANTFORMAT);
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-sladdet-happy.json");

		ResponseEntity<String> responseEntity = callHentDokument(createHeaders(), null);

		assertOkSladdetResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + SLADDET_VARIANTFORMAT)));
	}

	@Test
	void shouldHentArkivDokumentWhenVariantFormatIsNotSpecifiedAndSladdetDoesNotExist() {
		tilgangskontrollPermit();
		stubHappyHentDokument();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-happy.json");
		stubPdl();

		ResponseEntity<String> responseEntity = callHentDokument(createHeaders(), null);

		assertOkArkivResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)));
	}

	@Test
	void shouldReturnNotFoundWhenVariantFormatIsNotSpecifiedAndNeitherSladdetNorArkivExists() {
		tilgangskontrollPermit();
		stubPdl();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-uten-sladdet-eller-arkiv.json");

		ResponseEntity<String> responseEntity = callHentDokument(createHeaders(), null);

		assertEquals(NOT_FOUND, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody())
				.contains("Dokument med journalpostId=%s og dokumentInfoId=%s har ingen SLADDET- eller ARKIV-variant for automatisk valg"
						.formatted(JOURNALPOST_ID, DOKUMENT_ID));
		verify(0, getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + ORIGINAL_VARIANTFORMAT)));
	}

	@Test
	void shouldHentOriginalDokumentWhenCallerIsSystemWithoutRoles() {
		stubPdl();
		stubHappyHentDokument(ORIGINAL_VARIANTFORMAT);
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-happy.json");

		ResponseEntity<String> responseEntity = callHentDokument(createHeadersClientCredentialWithoutRoles(), ORIGINAL);
		assertOkOriginalResponse(responseEntity);

		verify(postRequestedFor(urlEqualTo("/pdl")));
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + ORIGINAL_VARIANTFORMAT)));
	}

	@Test
	void shouldNotHentDokumentWhenBrukerErOrganisasjonAndNotEgenAnsatt() {
		tilgangskontrollPermit();
		stubNavHrOrganisasjonJa(ORG_NR);
		stubMsGraphMemberOfNoGroupsDefaultSaksbehandler();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-org-happy.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getBody()).contains("Journalpost/dokument er knyttet til organisasjon underlagt NAV og det krever egen ansatt behandling for oppslag på denne.");
	}

	@Test
	void shouldGetForbiddenFromPep1g() {
		tilgangskontrollDenyPep1g();
		stubPdl();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-happy.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		verify(postRequestedFor(urlEqualTo("/pdl")));
		verifyTilgangsmaskinenDenyPep1gAndHttpStatusCode(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains(PEP1G_DENY_REASON);
	}

	@Test
	void shouldGetForbiddenFromPep2() {
		tilgangskontrollDenyPep2();
		stubPdl();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-tema-far.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		verify(postRequestedFor(urlEqualTo("/pdl")));
		tilgangskontrollDenyPep2();
		assertThat(responseEntity.getBody()).contains(PEP2_DENY_REASON);
	}

	@Test
	@DisplayName("Skal ikke hente farskap dokument hvis journalpost ikke har sakstilknytning eller bruker")
	void shouldGetForbiddenFromPep2WhenMidlertidigJournalpost() {
		tilgangskontrollDenyPep2();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-midlertidig-ingen-bruker-tema-far.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains(PEP2_DENY_REASON);
	}

	@Test
	void shouldGetForbiddenFromPep2d() {
		tilgangskontrollDenyPep2d();
		stubHappyBisysSak();
		stubPdl();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-tema-bid.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		verify(postRequestedFor(urlEqualTo("/pdl")));
		assertEquals(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains("Saksbehandler har ikke tilgang til tema ressursen tilhører. Saksbehandler må være i gruppen 0000-GA-TEMA_BID i Entra ID.");
	}

	@Test
	void shouldGetForbiddenFromPep8d() {
		tilgangskontrollDenyPep8d();
		stubHappyBisysSak();
		stubPdl();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-avsluttet-sak.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		verify(postRequestedFor(urlEqualTo("/pdl")));
		assertEquals(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains("Saksbehandler har ikke tilgang til ressurs som er tilknyttet en avsluttet sak");
	}

	@Test
	void shouldNotHentDokumentWhenBrukerErOrganisasjonAndNotEgenAnsattWithNavUserIdHeader() {
		tilgangskontrollPermit();
		stubNavHrOrganisasjonJa(ORG_NR);
		stubMsGraphMemberOfNoGroupsDefaultSaksbehandler();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-org-happy.json");

		ResponseEntity<String> responseEntity = callHentDokumentNavUserIdHeader();

		assertThat(responseEntity.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(responseEntity.getBody()).contains("Journalpost/dokument er knyttet til organisasjon underlagt NAV og det krever egen ansatt behandling for oppslag på denne.");
	}

	@Test
	void shouldReturnForbiddenForHentDokumentWhenSystemAndNavUserIdHeader() {
		var response = callHentDokumentNavUserIdHeaderWithoutRoles();

		assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(response.getBody()).contains("Tilgang er avvist. Tjeneste kalt med Nav-User-Id header og maskin-til-maskin Entra token krever role=tilgang_nav_user_id_header.");
	}

	@Test
	void shouldGetForbiddenFromPep1gWithNavUserIdHeader() {
		tilgangskontrollDenyPep1g();
		stubPdl();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-happy.json");

		ResponseEntity<String> responseEntity = callHentDokumentNavUserIdHeader();

		verify(postRequestedFor(urlEqualTo("/pdl")));
		verifyTilgangsmaskinenDenyPep1gAndHttpStatusCode(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains(PEP1G_DENY_REASON);
	}

	@Test
	void shouldGetForbiddenFromPep2WithNavUserIdHeader() {
		tilgangskontrollDenyPep2();
		stubPdl();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-tema-far.json");

		ResponseEntity<String> responseEntity = callHentDokumentNavUserIdHeader();

		verify(postRequestedFor(urlEqualTo("/pdl")));
		tilgangskontrollDenyPep2();
		assertThat(responseEntity.getBody()).contains(PEP2_DENY_REASON);
	}

	@Test
	@DisplayName("Skal ikke hente farskap dokument hvis journalpost ikke har sakstilknytning eller bruker")
	void shouldGetForbiddenFromPep2WhenMidlertidigJournalpostWithNavUserIdHeader() {
		tilgangskontrollDenyPep2();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-midlertidig-ingen-bruker-tema-far.json");

		ResponseEntity<String> responseEntity = callHentDokumentNavUserIdHeader();

		assertEquals(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains(PEP2_DENY_REASON);
	}

	@Test
	void shouldGetForbiddenFromPep2dWithNavUserIdHeader() {
		tilgangskontrollDenyPep2d();
		stubHappyBisysSak();
		stubPdl();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-tema-bid.json");

		ResponseEntity<String> responseEntity = callHentDokumentNavUserIdHeader();

		verify(postRequestedFor(urlEqualTo("/pdl")));
		assertEquals(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains("Saksbehandler har ikke tilgang til tema ressursen tilhører. Saksbehandler må være i gruppen 0000-GA-TEMA_BID i Entra ID.");
	}

	@Test
	void shouldGetForbiddenFromPep8dWithNavUserIdHeader() {
		tilgangskontrollDenyPep8d();
		stubHappyBisysSak();
		stubPdl();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-avsluttet-sak.json");

		ResponseEntity<String> responseEntity = callHentDokumentNavUserIdHeader();

		verify(postRequestedFor(urlEqualTo("/pdl")));
		assertEquals(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains("Saksbehandler har ikke tilgang til ressurs som er tilknyttet en avsluttet sak");
	}

	@Test
	void shouldNotHentArkivDokumentWhenCallerIsSystemWithoutTemaRole() {
		stubPdl();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-happy.json");

		ResponseEntity<String> responseEntity = callHentDokument(createHeadersClientCredentialWithoutRoles());
		assertEquals(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains("System har ikke tilgang til tema ressursen tilhører");

		verify(postRequestedFor(urlEqualTo("/pdl")));
	}

	@Test
	void shouldNotReturnDokumentWhenPensjonHentBrukerForSakTechnicalError() {
		tilgangskontrollPermit();
		stubDokarkivJournalpost("journalpost-dokumentinfo-pensjon-happy.json");
		stubFor(get(PENSJON_API_PIP_HENT_BRUKER_OG_ENHETSTILGANGER_FOR_SAK_V1_URL)
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void shouldNotReturnDokumentWhenPensjonHentBrukerForSakFunctionalErrorEmptyResponse() {
		tilgangskontrollPermit();
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
		tilgangskontrollPermit();
		stubDokarkivJournalpost("journalpost-dokumentinfo-pensjon-happy.json");
		stubFor(get(PENSJON_API_PIP_HENT_BRUKER_OG_ENHETSTILGANGER_FOR_SAK_V1_URL)
				.willReturn(aResponse().withStatus(HttpStatus.UNAUTHORIZED.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void shouldReturnNotFoundWhenDokumentNotFound() {
		tilgangskontrollPermit();
		stubPdl();
		stubFor(get("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(NOT_FOUND.value())));
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-happy.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		verify(postRequestedFor(urlEqualTo("/pdl")));
		assertEquals(NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void shouldReturnInternalServerErrorWhenHentDokumentJoarkTechnicalFail() {
		tilgangskontrollPermit();
		stubFor(get("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())));
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-happy.json");

		ResponseEntity<String> responseEntity = callHentDokument();
		assertEquals(INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
	}

	@Test
	void shouldReturnInternalServerErrorWhenHentJournalpostTechnicalFail() {
		tilgangskontrollPermit();
		stubDokarkivJournalpost(INTERNAL_SERVER_ERROR);

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
	}

	@Test
	void shouldReturnNotFoundWhenOriginalVariantDoesNotExist() {
		tilgangskontrollPermit();
		stubPdl();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-dokumentvariant-notmatched.json");

		ResponseEntity<String> responseEntity = callHentDokument(ORIGINAL);

		verify(postRequestedFor(urlEqualTo("/pdl")));
		assertEquals(NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void shouldReturnBadRequestWhenDokumentVariantIsWrong() {
		tilgangskontrollPermit();
		stubPdl();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-dokumentvariant-notmatched.json");

		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + "/" + "ugyldigVariantFormat";

		ResponseEntity<String> responseEntity = this.restTemplate.exchange(RequestEntity.get(URI.create(uri)).headers(createHeaders()).build(), String.class);

		assertEquals(BAD_REQUEST, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains("Ugyldig variantFormat");
	}

	@Test
	void shouldReturnNotFoundWhenJournalpostMetadataNotFound() {
		tilgangskontrollPermit();
		stubDokarkivJournalpost(NOT_FOUND);

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(NOT_FOUND, responseEntity.getStatusCode());
	}

	@Test
	void shouldReturnInternalServerErrorWhenJournalpostMetadataBadRequest() {
		tilgangskontrollPermit();
		stubDokarkivJournalpost(BAD_REQUEST);

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
	}

	@Test
	void shouldHentDokumentWhenHappyAndInJoarkHistoriskAvsluttetSak() {
		tilgangskontrollPermit();
		stubHappyHentDokument();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-avsluttet-sak.json");
		stubPdl();

		ListAppender<ILoggingEvent> listAppender = initialiseLogAppender();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)));
		verify(postRequestedFor(urlEqualTo("/pdl")));

		List<String> auditLog = listAppender.list.stream().map(ILoggingEvent::getMessage).toList();
		assertThat(auditLog).hasSize(1);

		String cefLogLine = auditLog.getFirst();
		assertThat(cefLogLine).startsWith("CEF:0|joark|saf_hentdokument|1.0|audit:access|Saksbehandler hentet dokument som gjelder bruker|INFO|");
		assertThat(cefLogLine).contains(
				"duid=" + USER_FNR_FROM_PDL,
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
	void shouldGetForbiddenFromPep3() {
		tilgangskontrollDenyPep3();
		stubHappyBisysSak();
		stubPdl();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-tema-bid.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		verify(postRequestedFor(urlEqualTo("/pdl")));
		assertEquals(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains(PEP3_DENY_REASON);
	}

	@Test
	void shouldGetForbiddenFromPep4WhenJournalstatusUtgaar() {
		tilgangskontrollDenyPep4();
		stubHappyBisysSak();
		stubPdl();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-tema-bid-utgaar.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		verify(postRequestedFor(urlEqualTo("/pdl")));
		assertEquals(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains(PEP4_DENY_REASON);
	}

	@Test
	void shouldGetForbiddenFromPep4WhenJournalpostSkjermet() {
		tilgangskontrollDenyPep4();
		stubHappyBisysSak();
		stubPdl();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-tema-bid-journalpost-skjerming.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		verify(postRequestedFor(urlEqualTo("/pdl")));
		assertEquals(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains(PEP4_DENY_REASON);
	}

	@Test
	void shouldGetForbiddenFromPep5WhenDokumentinfoSkjermet() {
		tilgangskontrollDenyPep5();
		stubHappyBisysSak();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-tema-bid-dokumentinfo-skjerming.json");
		stubPdl();

		ResponseEntity<String> responseEntity = callHentDokument();

		verify(postRequestedFor(urlEqualTo("/pdl")));
		assertEquals(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains(PEP5_DENY_REASON);
	}

	@Test
	void shouldGetForbiddenFromPep6d() {
		tilgangskontrollDenyPep6dWithSkjerming();
		stubHappyHentDokument();
		stubHappyBisysSak();
		stubPdl();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-tema-bid-fildetaljer-skjerming.json");

		ResponseEntity<String> responseEntity = callHentDokument();

		verify(postRequestedFor(urlEqualTo("/pdl")));
		assertEquals(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains(PEP6D_DENY_REASON);
	}

	@Test
	void shouldGetForbiddenFromPep7dWhenTemaForeldrepenger() {
		tilgangskontrollDenyPep7d();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-tema-for.json");
		stubFor(get("/fpsak?saksnummer=FOR2000")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("fpsak/happy-response.json")));
		stubPdl();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains(PEP7D_DENY_REASON);
		verify(postRequestedFor(urlEqualTo("/pdl")));
	}

	@Test
	void shouldGetForbiddenFromPep7dWhenK9TemaFri() {
		tilgangskontrollDenyPep7d();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-tema-fri.json");
		stubFor(get("/k9sak?saksnummer=K92000")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("k9/happy-response.json")));
		stubPdl();

		ResponseEntity<String> responseEntity = callHentDokument();

		assertEquals(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains(PEP7D_DENY_REASON);
		verify(postRequestedFor(urlEqualTo("/pdl")));
	}

	@Test
	void shouldGetForbiddenFromPep7ForK9TemaOms() {
		tilgangskontrollDenyPep7d();
		stubPdl();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-tema-oms.json");
		stubFor(get("/k9sak?saksnummer=K92000")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("k9/happy-response.json")));

		ResponseEntity<String> responseEntity = callHentDokument();

		verify(postRequestedFor(urlEqualTo("/pdl")));
		assertEquals(FORBIDDEN, responseEntity.getStatusCode());
		assertThat(responseEntity.getBody()).contains(PEP7D_DENY_REASON);
	}


	@Test
	void shouldHentDokumentAlsoWhenUserNotFoundInPDL() {
		tilgangskontrollPermit();
		stubHappyHentDokument();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-happy.json");
		stubPdl("pdl-person-ikke-funnet.json");

		Logger logger = (Logger) LoggerFactory.getLogger("hentdokument_sporbarhetslogg");
		ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
		listAppender.start();
		logger.addAppender(listAppender);

		ResponseEntity<String> responseEntity = callHentDokument();

		assertOkArkivResponse(responseEntity);
		verify(getRequestedFor(urlEqualTo("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + VARIANTFORMAT)));
		verify(postRequestedFor(urlEqualTo("/pdl")));

		List<String> auditLog = listAppender.list.stream().map(ILoggingEvent::getMessage).toList();
		assertThat(auditLog).hasSize(1);

		String cefLogLine = auditLog.getFirst();
		assertThat(cefLogLine).startsWith("CEF:0|joark|saf_hentdokument|1.0|audit:access|Saksbehandler hentet dokument som gjelder bruker|INFO|");
		assertThat(cefLogLine).contains(
				"duid=" + USER_FNR_FROM_T_BRUKER,
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
	void shouldAuditLogWhenNavUserIdHeaderAndSystemTilSystem() {
		tilgangskontrollPermit();
		stubPdl();
		stubHappyHentDokument();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-happy.json");

		ListAppender<ILoggingEvent> listAppender = initialiseLogAppender();

		ResponseEntity<String> responseEntity = callHentDokumentNavUserIdHeader();
		assertOkArkivResponse(responseEntity);

		List<String> auditLog = listAppender.list.stream().map(ILoggingEvent::getMessage).toList();
		assertThat(auditLog).hasSize(1);

		String cefLogLine = auditLog.getFirst();
		assertThat(cefLogLine).startsWith("CEF:0|joark|saf_hentdokument|1.0|audit:access|Saksbehandler hentet dokument som gjelder bruker|INFO|");
		assertThat(cefLogLine).contains(
				"duid=" + USER_FNR_FROM_PDL,
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
	void shouldNotAuditLogWhenSystemTilSystem() {
		tilgangskontrollPermit();
		stubPdl();
		stubHappyHentDokument();
		stubDokarkivJournalpost("journalpost-dokumentinfo-sak-happy.json");

		ListAppender<ILoggingEvent> listAppender = initialiseLogAppender();

		ResponseEntity<String> responseEntity = callHentDokument(createHeadersClientCredential());
		assertOkArkivResponse(responseEntity);

		List<String> auditLog = listAppender.list.stream().map(ILoggingEvent::getMessage).toList();
		assertThat(auditLog).hasSize(0);
	}

	private static void stubHappyHentDokument() {
		stubHappyHentDokument(VARIANTFORMAT);
	}

	private static void stubHappyHentDokument(VariantFormatCode variantFormat) {
		stubFor(get("/dokarkiv/hentdokument/" + DOKUMENT_ID + "/" + variantFormat)
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
		stubFor(get("/bidrag/v2/pip/sak/BISYS2000")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("bidrag/bidragsak-happy.json")));
	}

	private static @NotNull ListAppender<ILoggingEvent> initialiseLogAppender() {
		Logger logger = (Logger) LoggerFactory.getLogger("hentdokument_sporbarhetslogg");
		ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
		listAppender.start();
		logger.addAppender(listAppender);
		return listAppender;
	}

	private void assertOkArkivResponse(ResponseEntity<String> responseEntity) {
		assertEquals(DOKUMENT_ID + "_" + VARIANTFORMAT + ".pdf", responseEntity.getHeaders().getContentDisposition().getFilename());
	}

	private void assertOkSladdetResponse(ResponseEntity<String> responseEntity) {
		assertEquals(DOKUMENT_ID + "_" + SLADDET_VARIANTFORMAT + ".pdf", responseEntity.getHeaders().getContentDisposition().getFilename());
		assertOkResponse(responseEntity);
	}

	private void assertOkOriginalResponse(ResponseEntity<String> responseEntity) {
		assertEquals(DOKUMENT_ID + "_" + ORIGINAL_VARIANTFORMAT + ".pdf", responseEntity.getHeaders().getContentDisposition().getFilename());
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
		return callHentDokument(createHeaders());
	}

	private ResponseEntity<String> callHentDokumentNavUserIdHeader() {
		return callHentDokument(createHeadersNavUserId());
	}

	private ResponseEntity<String> callHentDokumentNavUserIdHeaderWithoutRoles() {
		return callHentDokument(createHeadersNavUserIdWithoutRoles());
	}

	private ResponseEntity<String> callHentDokument(HttpHeaders headers) {
		return callHentDokument(headers, VARIANTFORMAT);
	}

	private ResponseEntity<String> callHentDokumentSladdetVariant() {
		return callHentDokument(SLADDET_VARIANTFORMAT);
	}

	private ResponseEntity<String> callHentDokument(VariantFormatCode variantFormat) {
		return callHentDokument(createHeaders(), variantFormat);
	}

	private ResponseEntity<String> callHentDokument(HttpHeaders headers, VariantFormatCode variantFormatCode) {
		String variantFormatSegment = variantFormatCode == null ? "" : "/" + variantFormatCode;
		String uri = "/rest/hentdokument/" + JOURNALPOST_ID + "/" + DOKUMENT_ID + variantFormatSegment;
		RequestEntity<Void> requestEntity = RequestEntity.get(URI.create(uri)).headers(headers).build();
		return this.restTemplate.exchange(requestEntity, String.class);
	}
}
