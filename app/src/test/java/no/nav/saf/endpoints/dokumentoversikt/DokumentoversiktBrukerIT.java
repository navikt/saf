package no.nav.saf.endpoints.dokumentoversikt;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.endpoints.AbstractItest;
import no.nav.saf.endpoints.graphql.GraphQLRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingXPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class DokumentoversiktBrukerIT extends AbstractItest {

	private static final String AKTOER_ID = "1912374211459";
	private static final String FNR = "11111111111";
	private static final String ORG_NR = "201545004";
	private static final String KANAL_REFERANSE_ID = "KANAL REFERANSE ID";

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldHentDokumentoversiktBrukerWithAktoerID() throws URISyntaxException {
		abacPermit();
		stubHappyPdl();
		stubHappyPsakWithBody();
		this.stubHappyXmlSts();
		this.stubHappyGsakWithBody("gsak/gsak-sakerBySaksId_not_bid-happy.json");
		this.stubHappyFinnjournalposterWithBody("joark/finnjournalposter-happy.json");

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
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
		verify(postRequestedFor(urlEqualTo("/pensjonsakv1")).withRequestBody(matchingXPath("//personident/text()", equalTo("11111111111"))));
	}

	@Test
	void shouldHentDokumentoversiktBrukerWithFNR() throws URISyntaxException {
		abacPermit();
		stubHappyPdl();
		stubHappyPsakWithBody();
		this.stubHappyXmlSts();
		this.stubHappyGsakWithBody("gsak/gsak-sakerBySaksId_not_bid-happy.json");
		this.stubHappyFinnjournalposterWithBody("joark/finnjournalposter-happy.json");

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithFnr();
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
		verify(postRequestedFor(urlEqualTo("/pensjonsakv1")).withRequestBody(matchingXPath("//personident/text()", equalTo("11111111111"))));
	}

	@Test
	void shouldHentDokumentoversiktBrukerWithOrgNr() throws URISyntaxException {
		abacPermit();
		this.stubHappyXmlSts();
		this.stubHappyFinnjournalposterWithBody("joark/finnjournalposter_single_temaForNullskjerming-happy.json");
		stubFor(get("/gsak?orgnr=" + ORG_NR)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("gsak/gsak-sakerBySaksId_not_bid-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithOrgnr();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(0).getEksternReferanseId());
		assertSaksbehandlerHarTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.gsakSakIds", containing("135695442"))));
		verify(0, postRequestedFor(urlEqualTo("/pensjonsakv1")));
	}

	@Test
	void shouldHentDokumentoversiktBrukerWithFraDato() throws URISyntaxException {
		abacPermit();
		stubHappyPdl();
		this.stubHappyXmlSts();
		this.stubHappyGsakWithBody("gsak/gsak-sakerBySaksId_not_bid-happy.json");
		this.stubHappyFinnjournalposterWithBody("joark/finnjournalposter-happy.json");

		Map<String, Object> variables = new HashMap<>();
		variables.put("fnr", "11111111111");
		variables.put("fraDato", "2020-06-23");

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithFraDato(variables);
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertFalse(dokumentoversikt.getJournalposter().isEmpty());
	}

	@Test
	void shouldHentDokumentoversiktBrukerWithAktoerIDMidlertidig() throws URISyntaxException {
		abacPermit();
		stubHappyPdl();
		stubHappyPsakWithEmptyList();
		this.stubHappyXmlSts();
		this.stubHappyGsakWithBody("gsak/gsak-sakerBySaksId-empty.json");
		this.stubHappyFinnjournalposterWithBody("joark/finnjournalposter-midlertidig-happy.json");

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals("429812815", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertSaksbehandlerHarTilgang(dokumentoversikt);
		verify(2, postRequestedFor(urlEqualTo("/abac")));
		verify(postRequestedFor(urlEqualTo("/pdl")).withRequestBody(matchingJsonPath("$.variables.ident", equalTo(AKTOER_ID))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
		verify(postRequestedFor(urlEqualTo("/pensjonsakv1")).withRequestBody(matchingXPath("//personident/text()", equalTo("11111111111"))));
	}

	@Test
	void shouldHentDokumentoversiktBrukerWithAktoerIDSladdet() throws URISyntaxException {
		abacPermit();
		stubHappyPdl();
		stubHappyPsakWithBody();
		this.stubHappyXmlSts();
		this.stubHappyGsakWithBody("gsak/gsak-sakerBySaksId_not_bid-happy.json");
		this.stubHappyFinnjournalposterWithBody("joark/finnjournalposter_single_sladdet-happy.json");

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertSaksbehandlerHarTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/pdl")).withRequestBody(matchingJsonPath("$.variables.ident", equalTo(AKTOER_ID))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[\"21998969\"],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
		verify(postRequestedFor(urlEqualTo("/pensjonsakv1")).withRequestBody(matchingXPath("//personident/text()", equalTo("11111111111"))));
	}

	@Test
	void hentIdentForAktoerIdTechnicalFail() throws URISyntaxException {
		this.stubPdlWithOkResponseWithBadBodyResponse();

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/pdl")).withRequestBody(matchingJsonPath("$.variables.ident", equalTo(AKTOER_ID))));
		verify(0, postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")));
		verify(0, postRequestedFor(urlEqualTo("/pensjonsakv1")));
	}

	@Test
	void hentIdentForAktoerIdFunctionalFail() throws URISyntaxException {
		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);
		this.stubPdlWithOkResponseWithBadBodyResponse();

		assertEquals(OK, responseEntity.getStatusCode());
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/pdl")).withRequestBody(matchingJsonPath("$.variables.ident", equalTo(AKTOER_ID))));
		verify(0, postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")));
		verify(0, postRequestedFor(urlEqualTo("/pensjonsakv1")));
	}

	@Test
	void HentSakerByAktoerIdGsakTechnicalFail() throws URISyntaxException {
		abacPermit();
		stubHappyPdl();
		stubHappyPsakWithBody();
		this.stubHappyXmlSts();
		this.stubHappyFinnjournalposterWithBody("joark/finnjournalposter-empty.json");
		stubFor(get("/gsak?aktoerId=" + AKTOER_ID)
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		assertEquals(OK, responseEntity.getStatusCode());
		verify(postRequestedFor(urlEqualTo("/pdl")).withRequestBody(matchingJsonPath("$.variables.ident", equalTo(AKTOER_ID))));
		verify(1, postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")));
		verify(postRequestedFor(urlEqualTo("/pensjonsakv1")).withRequestBody(matchingXPath("//personident/text()", equalTo("11111111111"))));
	}

	@Test
	void HentSakerByAktoerIdGsakFunctionalFail() throws URISyntaxException {
		abacPermit();
		stubHappyPdl();
		stubHappyPsakWithBody();
		this.stubHappyXmlSts();
		this.stubHappyFinnjournalposterWithBody("joark/finnjournalposter-empty.json");
		this.stubHappyBidrag();
		stubFor(get("/gsak?aktoerId=" + AKTOER_ID)
				.willReturn(aResponse().withStatus(NOT_FOUND.value())));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		assertEquals(OK, responseEntity.getStatusCode());
		verify(postRequestedFor(urlEqualTo("/pdl")).withRequestBody(matchingJsonPath("$.variables.ident", equalTo(AKTOER_ID))));
		verify(1, postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")));
		verify(postRequestedFor(urlEqualTo("/pensjonsakv1")).withRequestBody(matchingXPath("//personident/text()", equalTo("11111111111"))));
	}

	@Test
	void bidragConsumerTechnicalError() throws URISyntaxException {
		abacPermit();
		stubHappyPdl();
		stubHappyPsakWithEmptyList();
		this.stubHappyXmlSts();
		this.stubHappyGsakWithBody("gsak/gsak-sakerBySaksId-happy.json");
		this.stubHappyFinnjournalposterWithBody("joark/finnjournalposter-empty.json");
		stubFor(get("/bidrag/654321").willReturn(aResponse()
				.withStatus(INTERNAL_SERVER_ERROR.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/pdl")).withRequestBody(matchingJsonPath("$.variables.ident", equalTo(AKTOER_ID))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.gsakSakIds", containing(""))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.psakSakIds", containing(""))));
		verify(postRequestedFor(urlEqualTo("/pensjonsakv1")).withRequestBody(matchingXPath("//personident/text()", equalTo("11111111111"))));
		verify(getRequestedFor(urlEqualTo("/bidrag/654321")));
	}

	@Test
	void shouldGetUnauthorizedFromPep1g() throws URISyntaxException {
		abacDenyPep1g();
		stubHappyPdl();
		stubHappyPsakWithBody();
		this.stubHappyXmlSts();
		this.stubHappyGsakWithBody("gsak/gsak-sakerBySaksId-happy.json");
		this.stubHappyFinnjournalposterWithBody("joark/finnjournalposter-happy.json");
		this.stubHappyBidrag();

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verifyabacDenyPep1gAndHttpStatusCode(OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep2() throws URISyntaxException {
		abacDenyPep2();
		stubHappyPdl();
		stubHappyPsakWithEmptyList();
		this.stubHappyXmlSts();
		this.stubHappyGsakWithBody("gsak/gsak-sakerByFagsakIdAndFagsaksystem-FAR-happy.json");
		this.stubHappyFinnjournalposterWithBody("joark/finnjournalposter-empty.json");

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verifyabacDenyPep2AndHttpStatusCode(OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep2d() throws URISyntaxException {
		abacDenyPep2dSkipPep2();
		stubHappyPdl();
		stubHappyPsakWithEmptyList();
		this.stubHappyXmlSts();
		this.stubHappyGsakWithBody("gsak/gsak-sakerBySaksId-happy.json");
		this.stubHappyFinnjournalposterWithBody("joark/finnjournalposter_single_bidragAndSkjerming-happy.json");
		this.stubHappyBidrag();

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertSaksbehandlerHarIkkeTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
		verifyabacDenyPep2dAndHttpStatusCode(true, OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep3() throws URISyntaxException {
		abacDenyPep3SkipPep2();
		stubHappyPdl();
		stubHappyPsakWithEmptyList();
		this.stubHappyXmlSts();
		this.stubHappyGsakWithBody("gsak/gsak-sakerBySaksId-happy.json");
		this.stubHappyFinnjournalposterWithBody("joark/finnjournalposter-empty.json");
		this.stubHappyBidrag();

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verifyabacDenyPep3ASkipPep2AndHttpStatusCode(OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep4() throws URISyntaxException {
		abacDenyPep4SkipPep2OrPep3();
		stubHappyPdl();
		stubHappyPsakWithEmptyList();
		this.stubHappyXmlSts();
		this.stubHappyGsakWithBody("gsak/gsak-sakerBySaksId-happy.json");
		this.stubHappyFinnjournalposterWithBody("joark/finnjournalposter_single_bidragAndSkjerming-happy.json");
		this.stubHappyBidrag();

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
		verifyabacDenyPep4SkipPep2OrPep3AndHttpStatusCode(OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep5() throws URISyntaxException {
		abacDenyPep5SkipPep2OrPep3();
		stubHappyPdl();
		stubHappyPsakWithEmptyList();
		this.stubHappyXmlSts();
		this.stubHappyGsakWithBody("gsak/gsak-sakerBySaksId-happy.json");
		this.stubHappyFinnjournalposterWithBody("joark/finnjournalposter_single_bidragAndSkjerming-happy.json");
		this.stubHappyBidrag();

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertFalse(dokumentoversikt.getJournalposter().isEmpty());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertTrue(dokumentoversikt.getJournalposter().get(0).getDokumenter().isEmpty());
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
		verifyabacDenyPep5SkipPep2OrPep3AndHttpStatusCode(true, OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep6d() throws URISyntaxException {
		abacDenyPep6dSkipPep2Pep3();
		stubHappyPdl();
		stubHappyPsakWithEmptyList();
		this.stubHappyXmlSts();
		this.stubHappyGsakWithBody("gsak/gsak-sakerBySaksId_not_bid-happy.json");
		this.stubHappyFinnjournalposterWithBody("joark/finnjournalposter_single_bidragAndSkjerming-happy.json");

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertSaksbehandlerHarIkkeTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
		verifyabacDenyPep6dSkipPep2Pep3AndHttpStatusCode(OK, responseEntity.getStatusCode());
	}

	private void stubPdlWithOkResponseWithBadBodyResponse(){
		stubFor(post("/pdl")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/badRequest.json")));
	}

	private void stubHappyBidrag() {
		stubFor(get("/bidrag/654321").willReturn(aResponse()
				.withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));
	}

	private void stubHappyXmlSts() {
		stubFor(post("/sts")
				.willReturn(aResponse().withStatus(OK.value())
						.withBodyFile("sts/sts-happy.xml")));
	}

	private void stubHappyGsakWithBody(String body) {
		stubFor(get("/gsak?aktoerId=" + AKTOER_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile(body)));
	}

	private void stubHappyFinnjournalposterWithBody(String body) {
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile(body)));
	}

	@Test
	void shouldGetUnauthorizedFromPep7d() throws URISyntaxException {
		String SAK_ID = "123456";
		abacDenyPep7dSkipPep2Pep3Pep4Pep5Pep6d();
		stubFor(post("/reststs")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("sts/sts-token.json")));
		stubFor(post("/pdl")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/hentPdlDataForIdent-happy.json")));
		stubFor(post("/sts")
				.willReturn(aResponse().withStatus(OK.value())
						.withBodyFile("sts/sts-happy.xml")));
		stubFor(get("/gsak?aktoerId=" + AKTOER_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("gsak/gsak-sakerBySaksId_oms-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter_single_temaK9Nullskjerming-happy.json")));
		stubFor(get("/k9sak?saksnummer=" + SAK_ID).willReturn(aResponse()
				.withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("k9/happy-response.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertSaksbehandlerHarIkkeTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
		verifyabacDenyPep7dSkipPep2Pep3Pep4Pep5Pep6dAndHttpStatusCode(OK, responseEntity.getStatusCode());
	}

	private ResponseEntity<LinkedHashMap> callDokumentOversikBrukerWithAktoerId() throws URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktBruker/dokumentoversiktbruker_aktoerid.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, LinkedHashMap.class);
	}

	private ResponseEntity<LinkedHashMap> callDokumentOversikBrukerWithFnr() throws URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktBruker/dokumentoversiktbruker_fnr.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, LinkedHashMap.class);
	}

	private ResponseEntity<LinkedHashMap> callDokumentOversikBrukerWithOrgnr() throws URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktBruker/dokumentoversiktbruker_orgnr.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, LinkedHashMap.class);
	}

	private Dokumentoversikt getDokumentoversikt(ResponseEntity<LinkedHashMap> responseEntity) {
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		return objectMapper.convertValue(responseEntityData.get("dokumentoversiktBruker"), Dokumentoversikt.class);
	}

	private ResponseEntity<LinkedHashMap> callDokumentOversikBrukerWithFraDato(Map<String, Object> variables) throws URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktBruker/dokumentoversiktbruker_literals.query"), null, variables);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, LinkedHashMap.class);
	}
}