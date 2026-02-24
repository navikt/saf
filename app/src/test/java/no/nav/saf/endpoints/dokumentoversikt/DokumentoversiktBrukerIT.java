package no.nav.saf.endpoints.dokumentoversikt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import no.nav.saf.domain.visningsmodell.DokumentInfo;
import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.endpoints.AbstractItest;
import no.nav.saf.endpoints.graphql.GraphQLRequest;
import no.nav.saf.endpoints.graphql.GraphQLResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static no.nav.saf.graphql.ErrorCode.FORBIDDEN;
import static no.nav.saf.tilgangskontroll.pep.DenyReasonCode.FORTROLIG_ADRESSE;
import static no.nav.saf.tilgangskontroll.pep.DenyReasonCode.ORGNR_NAV_STAT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class DokumentoversiktBrukerIT extends AbstractItest {

	private static final String FNR = "11111111111";
	private static final String KANAL_REFERANSE_ID = "KANAL REFERANSE ID";

	private final ObjectMapper objectMapper = new ObjectMapper();


	@BeforeEach
	public void setup() {
		setupHappyPathAzureToken();
		stubTexasToken();
	}

	@Test
	void shouldHentDokumentoversiktBrukerWithAktoerID() throws URISyntaxException {
		tilgangskontrollPermit();
		stubPdl();
		stubSakMedAktoerId();
		stubFinnjournalposter();
		stubPensjonSakSammendrag();

		ResponseEntity<GraphQLResponse> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);
		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals("429837329", dokumentoversikt.getJournalposter().get(1).getJournalpostId());
		assertEquals("429812815", dokumentoversikt.getJournalposter().get(2).getJournalpostId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(0).getEksternReferanseId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(1).getEksternReferanseId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(2).getEksternReferanseId());
		assertSaksbehandlerHarTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/pdl")).withRequestBody(matchingJsonPath("$.variables.ident", equalTo(AKTOER_ID))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[\"21998969\"],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
		verify(getRequestedFor(urlEqualTo(PENSJON_API_SAK_SAMMENDRAG_URL)).withHeader("fnr", new EqualToPattern(FNR)));
	}

	@Test
	void shouldHentDokumentoversiktBrukerWithAktoerIDAndHavDokumentInfosInCorrectOrder() throws URISyntaxException {
		tilgangskontrollPermit();
		stubPdl();
		stubSakMedAktoerId();
		// Merk: i filen under kommer vedleggene i feil rekkefølge "fra dokarkiv" for å teste at saf sorterer dem riktig.
		// i realiteten kommer de alltid i rekkefølge fra dokarkiv, men *kan komme* i uorden internt i saf
		stubFinnjournalposter("finnjournalposter-happy-mangevedlegg.json");
		stubPensjonSakSammendrag();

		ResponseEntity<GraphQLResponse> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);
		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().getFirst().getJournalpostId());
		assertEquals("441828515", dokumentoversikt.getJournalposter().getFirst().getDokumenter().get(0).getDokumentInfoId());
		assertEquals("441828513", dokumentoversikt.getJournalposter().getFirst().getDokumenter().get(1).getDokumentInfoId());
		assertEquals("441828512", dokumentoversikt.getJournalposter().getFirst().getDokumenter().get(2).getDokumentInfoId());
		assertEquals("441828511", dokumentoversikt.getJournalposter().getFirst().getDokumenter().get(3).getDokumentInfoId());
		assertEquals("429837329", dokumentoversikt.getJournalposter().get(1).getJournalpostId());
		assertEquals("429812815", dokumentoversikt.getJournalposter().get(2).getJournalpostId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(0).getEksternReferanseId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(1).getEksternReferanseId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(2).getEksternReferanseId());
		assertSaksbehandlerHarTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/pdl")).withRequestBody(matchingJsonPath("$.variables.ident", equalTo(AKTOER_ID))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[\"21998969\"],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
		verify(getRequestedFor(urlEqualTo(PENSJON_API_SAK_SAMMENDRAG_URL)).withHeader("fnr", new EqualToPattern(FNR)));
	}

	@Test
	void shouldHentDokumentoversiktBrukerWithSensitivtPselv() throws URISyntaxException {
		tilgangskontrollPermit();
		stubPdl();
		stubSakMedAktoerId();
		stubFinnjournalposter();
		stubPensjonSakSammendrag();

		ResponseEntity<GraphQLResponse> responseEntity = callDokumentOversikBrukerWithAktoerIdInkluderSensitivtPselv();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);
		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals(true, dokumentoversikt.getJournalposter().getFirst().getDokumenter().getFirst().getSensitivtPselv());
		assertEquals("429837329", dokumentoversikt.getJournalposter().get(1).getJournalpostId());
		assertFalse(dokumentoversikt.getJournalposter().get(1).getDokumenter().getFirst().getSensitivtPselv());
		assertEquals("429812815", dokumentoversikt.getJournalposter().get(2).getJournalpostId());
		assertFalse(dokumentoversikt.getJournalposter().get(2).getDokumenter().getFirst().getSensitivtPselv());
		assertSaksbehandlerHarTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/pdl")).withRequestBody(matchingJsonPath("$.variables.ident", equalTo(AKTOER_ID))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[\"21998969\"],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
		verify(getRequestedFor(urlEqualTo(PENSJON_API_SAK_SAMMENDRAG_URL)).withHeader("fnr", new EqualToPattern(FNR)));
	}

	@Test
	void shouldHentDokumentoversiktBrukerWithFNR() throws URISyntaxException {
		tilgangskontrollPermit();
		stubPdl();
		stubSakMedAktoerId();
		stubFinnjournalposter();
		stubPensjonSakSammendrag();

		ResponseEntity<GraphQLResponse> responseEntity = callDokumentOversikBrukerWithFnr();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals("429837329", dokumentoversikt.getJournalposter().get(1).getJournalpostId());
		assertEquals("429812815", dokumentoversikt.getJournalposter().get(2).getJournalpostId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(0).getEksternReferanseId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(1).getEksternReferanseId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(2).getEksternReferanseId());
		assertSaksbehandlerHarTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/pdl")).withRequestBody(matchingJsonPath("$.variables.ident", equalTo(FNR))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[\"21998969\"],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
		verify(getRequestedFor(urlEqualTo(PENSJON_API_SAK_SAMMENDRAG_URL)).withHeader("fnr", new EqualToPattern(FNR)));
	}

	@Test
	void shouldHentDokumentoversiktBrukerWithOrgNr() throws URISyntaxException {
		tilgangskontrollPermit();
		stubNavHrOrganisasjonNei(ORG_NR);
		stubSakOrgnr();
		stubFinnjournalposter("finnjournalposter_single_temaForNullskjerming-happy.json");

		ResponseEntity<GraphQLResponse> responseEntity = callDokumentOversikBrukerWithOrgnr();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(0).getEksternReferanseId());
		assertSaksbehandlerHarTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.gsakSakIds", containing("135695442"))));
		verify(0, getRequestedFor(urlEqualTo(PENSJON_API_SAK_SAMMENDRAG_URL)));
	}

	@Test
	void shouldNotHentDokumentoversiktBrukerWithNavStatOrgnummerWhenBrukerNotEgenAnsattBehandler() throws URISyntaxException {
		tilgangskontrollPermit();
		stubNavHrOrganisasjonJa(ORG_NR);
		stubMsGraphMemberOfNoGroupsDefaultSaksbehandler();

		ResponseEntity<GraphQLResponse> responseEntity = callDokumentOversikBrukerWithOrgnr();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		List<GraphQLResponse.Error> errors = responseEntity.getBody().getErrors();
		assertThat(errors.get(0).getExtensions().getCode()).isEqualTo(FORBIDDEN.getText());
		assertThat(errors.get(0).getExtensions().getReasonCode()).isEqualTo(ORGNR_NAV_STAT.code);
	}

	@Test
	void shouldHentDokumentoversiktBrukerWithNavStatOrgnummerWhenBrukerIsEgenAnsattBehandler() throws URISyntaxException {
		tilgangskontrollPermit();
		stubNavHrOrganisasjonJa(ORG_NR);
		stubSakOrgnr();
		stubFinnjournalposter("finnjournalposter_single_temaForNullskjerming-happy.json");

		ResponseEntity<GraphQLResponse> responseEntity = callDokumentOversikBrukerWithOrgnr();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(0).getEksternReferanseId());
		assertSaksbehandlerHarTilgang(dokumentoversikt);
		verifyMsGraphMemberOfSeveralGroupsCalled(MS_ID_SAKSBEHANDLER, 1);
	}

	@Test
	void shouldHentDokumentoversiktBrukerWithFraDato() throws URISyntaxException {
		tilgangskontrollPermit();
		stubPdl();
		stubSakMedAktoerId();
		stubFinnjournalposter();

		Map<String, Object> variables = new HashMap<>();
		variables.put("fnr", "11111111111");
		variables.put("fraDato", "2020-06-23");

		ResponseEntity<GraphQLResponse> responseEntity = callDokumentOversikBrukerWithFraDato(variables);
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertThat(dokumentoversikt.getJournalposter()).isNotEmpty();
	}

	@Test
	void shouldHentDokumentoversiktBrukerWithAktoerIDMidlertidig() throws URISyntaxException {
		tilgangskontrollPermit();
		stubPdl();
		stubSakMedAktoerId("sak-sakerBySaksId-empty.json");
		stubFinnjournalposter("finnjournalposter-midlertidig-happy.json");
		stubPensjonSakSammendrag("psak-hentSakSammendragListe-happy-empty.json");

		ResponseEntity<GraphQLResponse> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		Journalpost journalpost = dokumentoversikt.getJournalposter().get(0);
		assertEquals("429812815", journalpost.getJournalpostId());
		assertSaksbehandlerHarTilgang(dokumentoversikt);
		assertThat(journalpost.getTittel()).isEqualTo("SØKNAD_FORELDREPENGER_FØDSEL");
		DokumentInfo dokumentInfo = journalpost.getDokumenter().get(0);
		assertThat(dokumentInfo.getTittel()).isEqualTo("Søknad om foreldrepenger ved fødsel");

		verify(postRequestedFor(urlEqualTo("/pdl")).withRequestBody(matchingJsonPath("$.variables.ident", equalTo(AKTOER_ID))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
		verify(getRequestedFor(urlEqualTo(PENSJON_API_SAK_SAMMENDRAG_URL)).withHeader("fnr", new EqualToPattern(FNR)));
	}

	@Test
	void shouldHentDokumentoversiktBrukerWithAktoerIDSladdet() throws URISyntaxException {
		tilgangskontrollPermit();
		stubPdl();
		stubSakMedAktoerId();
		stubFinnjournalposter("finnjournalposter_single_sladdet-happy.json");
		stubPensjonSakSammendrag();

		ResponseEntity<GraphQLResponse> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertSaksbehandlerHarTilgang(dokumentoversikt);
		verify(getRequestedFor(urlEqualTo(PENSJON_API_SAK_SAMMENDRAG_URL)).withHeader("fnr", new EqualToPattern(FNR)));
		verify(postRequestedFor(urlEqualTo("/pdl")).withRequestBody(matchingJsonPath("$.variables.ident", equalTo(AKTOER_ID))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[\"21998969\"],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
	}

	@Test
	void hentIdentForAktoerIdTechnicalFail() throws URISyntaxException {
		stubPdl("badRequest.json");

		ResponseEntity<GraphQLResponse> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/pdl")).withRequestBody(matchingJsonPath("$.variables.ident", equalTo(AKTOER_ID))));
		verify(0, postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")));
		verify(0, getRequestedFor(urlEqualTo(PENSJON_API_SAK_SAMMENDRAG_URL)));
	}

	@Test
	void hentIdentForAktoerIdFunctionalFail() throws URISyntaxException {
		stubPdl("badRequest.json");

		ResponseEntity<GraphQLResponse> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/pdl")).withRequestBody(matchingJsonPath("$.variables.ident", equalTo(AKTOER_ID))));
		verify(0, postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")));
		verify(0, getRequestedFor(urlEqualTo(PENSJON_API_SAK_SAMMENDRAG_URL)));
	}

	@Test
	void HentSakerByAktoerIdGsakTechnicalFail() throws URISyntaxException {
		tilgangskontrollPermit();
		stubPdl();
		stubFor(get(SAK_API_PATH_MED_QUERY_PARA_AKTOER_ID)
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())));
		stubFinnjournalposter("finnjournalposter-empty.json");
		stubPensjonSakSammendrag();

		ResponseEntity<GraphQLResponse> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		assertEquals(OK, responseEntity.getStatusCode());
		verify(postRequestedFor(urlEqualTo("/pdl")).withRequestBody(matchingJsonPath("$.variables.ident", equalTo(AKTOER_ID))));
		verify(1, postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")));
		verify(getRequestedFor(urlEqualTo(PENSJON_API_SAK_SAMMENDRAG_URL)).withHeader("fnr", new EqualToPattern(FNR)));
	}

	@Test
	void HentSakerByAktoerIdGsakFunctionalFail() throws URISyntaxException {
		tilgangskontrollPermit();
		stubPdl();
		stubFor(get(SAK_API_PATH_MED_QUERY_PARA_AKTOER_ID)
				.willReturn(aResponse().withStatus(NOT_FOUND.value())));
		stubFinnjournalposter("finnjournalposter-empty.json");
		stubPensjonSakSammendrag();
		stubFor(get("/bidrag/*").willReturn(aResponse()
				.withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

		ResponseEntity<GraphQLResponse> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		assertEquals(OK, responseEntity.getStatusCode());
		verify(postRequestedFor(urlEqualTo("/pdl")).withRequestBody(matchingJsonPath("$.variables.ident", equalTo(AKTOER_ID))));
		verify(1, postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")));
		verify(getRequestedFor(urlEqualTo(PENSJON_API_SAK_SAMMENDRAG_URL)).withHeader("fnr", new EqualToPattern(FNR)));
	}

	@Test
	void bidragConsumerTechnicalError() throws URISyntaxException {
		tilgangskontrollPermit();
		stubPdl();
		stubSakMedAktoerId("sak-sakerBySaksId-happy.json");
		stubFinnjournalposter("finnjournalposter-empty.json");
		stubPensjonSakSammendrag("psak-hentSakSammendragListe-happy-empty.json");
		stubFor(get("/bidrag/654321").willReturn(aResponse()
				.withStatus(INTERNAL_SERVER_ERROR.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

		ResponseEntity<GraphQLResponse> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/pdl")).withRequestBody(matchingJsonPath("$.variables.ident", equalTo(AKTOER_ID))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[]")));
		verify(getRequestedFor(urlEqualTo(PENSJON_API_SAK_SAMMENDRAG_URL)).withHeader("fnr", new EqualToPattern(FNR)));
		verify(getRequestedFor(urlEqualTo("/bidrag/654321")));
	}

	@Test
	void shouldGetUnauthorizedFromPep1g() throws URISyntaxException {
		tilgangskontrollDenyPep1g();
		stubEntraProxy();
		stubPdl();
		stubSakMedAktoerId("sak-sakerBySaksId-happy.json");
		stubFinnjournalposter();
		stubPensjonSakSammendrag();
		stubBidrag();

		ResponseEntity<GraphQLResponse> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verifyTilgangsmaskinenDenyPep1gAndHttpStatusCode(OK, responseEntity.getStatusCode());
		List<GraphQLResponse.Error> errors = responseEntity.getBody().getErrors();
		assertThat(errors.get(0).getExtensions().getCode()).isEqualTo(FORBIDDEN.getText());
		assertThat(errors.get(0).getExtensions().getReasonCode()).isEqualTo(FORTROLIG_ADRESSE.code);
	}

	@Test
	void shouldGetUnauthorizedFromPep2() throws URISyntaxException {
		tilgangskontrollDenyPep2();
		stubPdl();
		stubSakMedAktoerId("sak-sakerByFagsakIdAndFagsaksystem-FAR-happy.json");
		stubFinnjournalposter("finnjournalposter-empty.json");
		stubPensjonSakSammendrag("psak-hentSakSammendragListe-happy-empty.json");

		ResponseEntity<GraphQLResponse> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verifyDenyPep2(responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep2WhenIngenSakstilknytning() throws URISyntaxException {
		tilgangskontrollDenyPep2();
		stubPdl();
		stubSakMedAktoerId("sak-sakerBySaksId-empty.json");
		stubFinnjournalposter("finnjournalposter-far-kta.json");
		stubPensjonSakSammendrag("psak-hentSakSammendragListe-happy-empty.json");

		ResponseEntity<GraphQLResponse> responseEntity = callDokumentOversikBrukerWithAktoerIdInkluderMidlertidige();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		assertEquals(OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldReturnSaksbehandlerHarTilgangFalseAndSkjultTittelWhenDenyPep2d() throws URISyntaxException {
		tilgangskontrollDenyPep2d();
		stubPdl();
		stubSakMedAktoerId("sak-sakerBySaksId-happy.json");
		stubFinnjournalposter("finnjournalposter_single_bidragAndSkjerming-happy.json");
		stubPensjonSakSammendrag("psak-hentSakSammendragListe-happy-empty.json");
		stubBidrag("bidragsak-happy.json");

		ResponseEntity<GraphQLResponse> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertSaksbehandlerHarIkkeTilgang(dokumentoversikt);
		assertSkjultTittel(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
		assertThat(responseEntity.getStatusCode()).isSameAs(HttpStatus.OK);
		verifyDenyPep2d(responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep3() throws URISyntaxException {
		tilgangskontrollDenyPep3();
		stubPdl();
		stubSakMedAktoerId("sak-sakerBySaksId-happy.json");
		stubFinnjournalposter("finnjournalposter-empty.json");
		stubPensjonSakSammendrag("psak-hentSakSammendragListe-happy-empty.json");
		stubBidrag();

		ResponseEntity<GraphQLResponse> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verifyEntraProxyCalled(1);
		assertEquals(OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep4() throws URISyntaxException {
		tilgangskontrollDenyPep4();
		stubPdl();
		stubSakMedAktoerId("sak-sakerBySaksId-happy.json");
		stubFinnjournalposter("finnjournalposter_single_bidragAndSkjerming-happy.json");
		stubPensjonSakSammendrag("psak-hentSakSammendragListe-happy-empty.json");
		stubBidrag();

		ResponseEntity<GraphQLResponse> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
		verifyDenyPep4(responseEntity.getStatusCode(), 1);
		verifyMsGraphMemberOfSeveralGroupsCalled(MS_ID_SAKSBEHANDLER, 1);
	}

	@Test
	void shouldGetUnauthorizedFromPep5() throws URISyntaxException {
		tilgangskontrollDenyPep5();
		stubPdl();
		stubSakMedAktoerId("sak-sakerBySaksId-happy.json");
		stubFinnjournalposter("finnjournalposter_single_bidragAndSkjermingOnlyDokument-happy.json");
		stubPensjonSakSammendrag("psak-hentSakSammendragListe-happy-empty.json");
		stubBidrag();

		ResponseEntity<GraphQLResponse> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertThat(dokumentoversikt.getJournalposter()).isNotEmpty();
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertThat(dokumentoversikt.getJournalposter().get(0).getDokumenter()).isEmpty();
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
		verifyDenyPep5(responseEntity.getStatusCode(), 1);
	}

	@Test
	void shouldGetUnauthorizedFromPep6d() throws URISyntaxException {
		tilgangskontrollDenyPep6dWithSkjerming();
		stubPdl();
		stubSakMedAktoerId();
		stubFinnjournalposter("finnjournalposter_single_bidragAndSkjermingOnlyDokvariant-happy.json");
		stubPensjonSakSammendrag("psak-hentSakSammendragListe-happy-empty.json");

		ResponseEntity<GraphQLResponse> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertSaksbehandlerHarIkkeTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
		verifyDenyPep6d(responseEntity.getStatusCode(), 1);
	}

	@Test
	void shouldGetUnauthorizedFromPep7d() throws URISyntaxException {
		String SAK_ID = "123456";
		tilgangskontrollDenyPep7d();
		stubPdl();
		stubSakMedAktoerId("sak-sakerBySaksId_oms-happy.json");
		stubFinnjournalposter("finnjournalposter_single_temaK9Nullskjerming-happy.json");
		stubFor(get("/k9sak?saksnummer=" + SAK_ID).willReturn(aResponse()
				.withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("k9/happy-response.json")));

		ResponseEntity<GraphQLResponse> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertSaksbehandlerHarIkkeTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
		verifyDenyPep7d(responseEntity.getStatusCode());
	}

	/*
	 * Totalt seks journalposter (totaltAntall i response fra finnJournalposter) returnes for gitt bruker, GraphQL-query
	 * spør etter første tre. En journalpost filtreres bort fra de første tre pga skjerming. Testen verifiserer at resultatet
	 * inneholder sideInfo hvor antall journalposter er 2, nesteSideFinnes er true, og dokumentoversikt med kun de to
	 * journalpostene uten skjerming.
	 */
	@Test
	void shouldReturnFinnesNesteSideTrueWhenDocumentsAreFilteredFromPageAndNesteSideExists() throws URISyntaxException {
		tilgangskontrollPermit();
		stubMsGraphMemberOfNoGroupsDefaultSaksbehandler();
		stubPdl();
		stubSakMedAktoerId();
		stubFinnjournalposter("finnjournalposter-paged-first-happy.json");
		stubPensjonSakSammendrag("psak-hentSakSammendragListe-happy-empty.json");

		ResponseEntity<GraphQLResponse> responseEntity = callDokumentOversikBrukerWithAktoerIdWithSideinfo();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(dokumentoversikt.getSideInfo().isFinnesNesteSide()).isTrue();
		assertThat(dokumentoversikt.getSideInfo().getAntall()).isEqualTo(2);
		assertThat(dokumentoversikt.getJournalposter())
				.hasSize(2)
				.extracting(Journalpost::getJournalpostId)
				.containsExactly("429837417", "429837329");
	}

	private ResponseEntity<GraphQLResponse> callDokumentOversikBrukerWithAktoerIdWithSideinfo() throws URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktBruker/dokumentoversiktbruker_aktoerid_with_sideinfo.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, GraphQLResponse.class);
	}

	private ResponseEntity<GraphQLResponse> callDokumentOversikBrukerWithAktoerId() throws URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktBruker/dokumentoversiktbruker_aktoerid.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, GraphQLResponse.class);
	}

	private ResponseEntity<GraphQLResponse> callDokumentOversikBrukerWithAktoerIdInkluderMidlertidige() throws URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktBruker/dokumentoversiktbruker_aktoerid_midlertidig.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, GraphQLResponse.class);
	}

	private ResponseEntity<GraphQLResponse> callDokumentOversikBrukerWithFnr() throws URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktBruker/dokumentoversiktbruker_fnr.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, GraphQLResponse.class);
	}

	private ResponseEntity<GraphQLResponse> callDokumentOversikBrukerWithAktoerIdInkluderSensitivtPselv() throws URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktBruker/dokumentoversiktbruker_aktoerid_sensitivtPselv.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, GraphQLResponse.class);
	}

	private ResponseEntity<GraphQLResponse> callDokumentOversikBrukerWithOrgnr() throws URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktBruker/dokumentoversiktbruker_orgnr.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, GraphQLResponse.class);
	}

	private Dokumentoversikt getDokumentoversikt(ResponseEntity<GraphQLResponse> responseEntity) {
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().getData().get("dokumentoversiktBruker");
		return objectMapper.convertValue(responseEntityData, Dokumentoversikt.class);
	}

	private ResponseEntity<GraphQLResponse> callDokumentOversikBrukerWithFraDato(Map<String, Object> variables) throws URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktBruker/dokumentoversiktbruker_literals.query"), null, variables);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, GraphQLResponse.class);
	}
}