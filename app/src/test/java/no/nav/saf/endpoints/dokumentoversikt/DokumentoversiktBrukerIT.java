package no.nav.saf.endpoints.dokumentoversikt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.endpoints.AbstractItest;
import no.nav.saf.endpoints.graphql.GraphQLRequest;
import org.apache.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
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
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
class DokumentoversiktBrukerIT extends AbstractItest {

	private static final String AKTOER_ID = "1912374211459";
	private static final String FNR = "11111111111";
	private static final String ORG_NR = "201545004";
	private static final String KANAL_REFERANSE_ID = "KANAL REFERANSE ID";

	private final ObjectMapper objectMapper = new ObjectMapper();


	@BeforeEach
	public void setyp () {
		stubFor(post("/azure_token")
				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response.json")));
	}
	@Test
	void shouldHentDokumentoversiktBrukerWithAktoerID() throws IOException, URISyntaxException {
		abacPermit();
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
						.withBodyFile("gsak/gsak-sakerBySaksId_not_bid-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter-happy.json")));
						stubFor(get("/pen/springapi/sak/sammendrag")
								.willReturn(aResponse().withStatus(OK.value())
										.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);
		assertEquals(OK, responseEntity.getStatusCode());
		verify(postRequestedFor(urlEqualTo("/azure_token")));
		verify(getRequestedFor(urlEqualTo("/pen/springapi/sak/sammendrag")).withHeader("fnr", new EqualToPattern(FNR)));
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
		verify(postRequestedFor(urlEqualTo("/reststs")));
		verify(postRequestedFor(urlEqualTo("/azure_token")));
		verify(getRequestedFor(urlEqualTo("/pen/springapi/sak/sammendrag")).withHeader("fnr", new EqualToPattern(FNR)));
	}

	@Test
	void shouldHentDokumentoversiktBrukerWithFNR() throws IOException, URISyntaxException {
		abacPermit();
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
						.withBodyFile("gsak/gsak-sakerBySaksId_not_bid-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter-happy.json")));
		stubFor(get("/pen/springapi/sak/sammendrag")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithFnr();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		verify(postRequestedFor(urlEqualTo("/azure_token")));
		verify(getRequestedFor(urlEqualTo("/pen/springapi/sak/sammendrag")).withHeader("fnr", new EqualToPattern(FNR)));
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
		verify(postRequestedFor(urlEqualTo("/reststs")));
	}

	@Test
	void shouldHentDokumentoversiktBrukerWithOrgNr() throws IOException, URISyntaxException {
		abacPermit();
		stubFor(post("/sts")
				.willReturn(aResponse().withStatus(OK.value())
						.withBodyFile("sts/sts-happy.xml")));
		stubFor(get("/gsak?orgnr=" + ORG_NR)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("gsak/gsak-sakerBySaksId_not_bid-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter_single_temaForNullskjerming-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithOrgnr();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(0).getEksternReferanseId());
		assertSaksbehandlerHarTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.gsakSakIds", containing("135695442"))));
		verify(0, getRequestedFor(urlEqualTo("/pen/springapi/sak/sammendrag")));
	}

	@Test
	void shouldHentDokumentoversiktBrukerWithFraDato() throws IOException, URISyntaxException {
		abacPermit();
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
						.withBodyFile("gsak/gsak-sakerBySaksId_not_bid-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter-happy.json")));

		Map<String, Object> variables = new HashMap<>();
		variables.put("fnr", "11111111111");
		variables.put("fraDato", "2020-06-23");

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithFraDato(variables);
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertFalse(dokumentoversikt.getJournalposter().isEmpty());
	}

	@Test
	void shouldHentDokumentoversiktBrukerWithAktoerIDMidlertidig() throws IOException, URISyntaxException {
		abacPermit();
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
				.willReturn(aResponse().withStatus(NOT_FOUND.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("gsak/gsak-sakerBySaksId-empty.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter-midlertidig-happy.json")));
						stubFor(get("/pen/springapi/sak/sammendrag")
								.willReturn(aResponse().withStatus(OK.value())
										.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-empty.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals("429812815", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertSaksbehandlerHarTilgang(dokumentoversikt);
		verify(2, postRequestedFor(urlEqualTo("/abac")));
		verify(postRequestedFor(urlEqualTo("/pdl")).withRequestBody(matchingJsonPath("$.variables.ident", equalTo(AKTOER_ID))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
		verify(getRequestedFor(urlEqualTo("/pen/springapi/sak/sammendrag")).withHeader("fnr", new EqualToPattern(FNR)));
	}

	@Test
	void shouldHentDokumentoversiktBrukerWithAktoerIDSladdet() throws IOException, URISyntaxException {
		abacPermit();
		stubFor(post("/azure_token")
				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response.json")));
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
						.withBodyFile("gsak/gsak-sakerBySaksId_not_bid-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter_single_sladdet-happy.json")));
		stubFor(get("/pen/springapi/sak/sammendrag")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertSaksbehandlerHarTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/reststs")));
		verify(postRequestedFor(urlEqualTo("/azure_token")));
		verify(getRequestedFor(urlEqualTo("/pen/springapi/sak/sammendrag")).withHeader("fnr", new EqualToPattern(FNR)));
		verify(postRequestedFor(urlEqualTo("/pdl")).withRequestBody(matchingJsonPath("$.variables.ident", equalTo(AKTOER_ID))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[\"21998969\"],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
	}

	@Test
	void hentIdentForAktoerIdTechnicalFail() throws IOException, URISyntaxException {
		stubFor(post("/reststs")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("sts/sts-token.json")));
		stubFor(post("/pdl")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/badRequest.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/pdl")).withRequestBody(matchingJsonPath("$.variables.ident", equalTo(AKTOER_ID))));
		verify(0, postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")));
		verify(0, getRequestedFor(urlEqualTo("/pen/springapi/sak/sammendrag")));
	}

	@Test
	void hentIdentForAktoerIdFunctionalFail() throws IOException, URISyntaxException {
		stubFor(post("/reststs")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("sts/sts-token.json")));
		stubFor(post("/pdl")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/badRequest.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/pdl")).withRequestBody(matchingJsonPath("$.variables.ident", equalTo(AKTOER_ID))));
		verify(0, postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")));
		verify(0, getRequestedFor(urlEqualTo("/pen/springapi/sak/sammendrag")));
	}

	@Test
	void HentSakerByAktoerIdGsakTechnicalFail() throws IOException, URISyntaxException {
		abacPermit();
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
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter-empty.json")));
		stubFor(get("/pen/springapi/sak/sammendrag")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		assertEquals(OK, responseEntity.getStatusCode());
		verify(postRequestedFor(urlEqualTo("/pdl")).withRequestBody(matchingJsonPath("$.variables.ident", equalTo(AKTOER_ID))));
		verify(1, postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")));
		verify(postRequestedFor(urlEqualTo("/reststs")));
		verify(postRequestedFor(urlEqualTo("/azure_token")));
		verify(getRequestedFor(urlEqualTo("/pen/springapi/sak/sammendrag")).withHeader("fnr", new EqualToPattern(FNR)));
	}

	@Test
	void HentSakerByAktoerIdGsakFunctionalFail() throws IOException, URISyntaxException {
		abacPermit();
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
				.willReturn(aResponse().withStatus(NOT_FOUND.value())));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter-empty.json")));
		stubFor(get("/pen/springapi/sak/sammendrag")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy.json")));
		stubFor(get("/bidrag/*").willReturn(aResponse()
				.withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		assertEquals(OK, responseEntity.getStatusCode());
		verify(postRequestedFor(urlEqualTo("/pdl")).withRequestBody(matchingJsonPath("$.variables.ident", equalTo(AKTOER_ID))));
		verify(1, postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")));
		verify(postRequestedFor(urlEqualTo("/reststs")));
		verify(postRequestedFor(urlEqualTo("/azure_token")));
		verify(getRequestedFor(urlEqualTo("/pen/springapi/sak/sammendrag")).withHeader("fnr", new EqualToPattern(FNR)));
	}

	@Test
	void bidragConsumerTechnicalError() throws IOException, URISyntaxException {
		abacPermit();
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
						.withBodyFile("gsak/gsak-sakerBySaksId-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter-empty.json")));
		stubFor(get("/pen/springapi/sak/sammendrag")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-empty.json")));
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
		verify(postRequestedFor(urlEqualTo("/reststs")));
		verify(postRequestedFor(urlEqualTo("/azure_token")));
		verify(getRequestedFor(urlEqualTo("/pen/springapi/sak/sammendrag")).withHeader("fnr", new EqualToPattern(FNR)));
		verify(getRequestedFor(urlEqualTo("/bidrag/654321")));
	}

	@Test
	void shouldGetUnauthorizedFromPep1g() throws IOException, URISyntaxException {
		abacDenyPep1g();
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
						.withBodyFile("gsak/gsak-sakerBySaksId-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter-happy.json")));
		stubFor(get("/pen/springapi/sak/sammendrag")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy.json")));
		stubFor(get("/bidrag/654321").willReturn(aResponse().withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verifyabacDenyPep1gAndHttpStatusCode(OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep2() throws IOException, URISyntaxException {
		abacDenyPep2();
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
						.withBodyFile("gsak/gsak-sakerByFagsakIdAndFagsaksystem-FAR-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter-empty.json")));
		stubFor(get("/pen/springapi/sak/sammendrag")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-empty.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verifyabacDenyPep2AndHttpStatusCode(OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep2d() throws IOException, URISyntaxException {
		abacDenyPep2dSkipPep2();
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
						.withBodyFile("gsak/gsak-sakerBySaksId-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter_single_bidragAndSkjerming-happy.json")));
		stubFor(get("/pen/springapi/sak/sammendrag")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-empty.json")));
		stubFor(get("/bidrag/654321").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertSaksbehandlerHarIkkeTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
		verifyabacDenyPep2dAndHttpStatusCode(true, OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep3() throws IOException, URISyntaxException {
		abacDenyPep3SkipPep2();
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
						.withBodyFile("gsak/gsak-sakerBySaksId-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter-empty.json")));
		stubFor(get("/pen/springapi/sak/sammendrag")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-empty.json")));
		stubFor(get("/bidrag/654321").willReturn(aResponse().withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verifyabacDenyPep3ASkipPep2AndHttpStatusCode(OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep4() throws IOException, URISyntaxException {
		abacDenyPep4SkipPep2OrPep3();
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
						.withBodyFile("gsak/gsak-sakerBySaksId-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter_single_bidragAndSkjerming-happy.json")));
		stubFor(get("/pen/springapi/sak/sammendrag")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-empty.json")));
		stubFor(get("/bidrag/654321").willReturn(aResponse().withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
		verifyabacDenyPep4SkipPep2OrPep3AndHttpStatusCode(OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep5() throws IOException, URISyntaxException {
		abacDenyPep5SkipPep2OrPep3();
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
						.withBodyFile("gsak/gsak-sakerBySaksId-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter_single_bidragAndSkjerming-happy.json")));
		stubFor(get("/pen/springapi/sak/sammendrag")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-empty.json")));
		stubFor(get("/bidrag/654321").willReturn(aResponse().withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

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
	void shouldGetUnauthorizedFromPep6d() throws IOException, URISyntaxException {
		abacDenyPep6dSkipPep2Pep3();
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
						.withBodyFile("gsak/gsak-sakerBySaksId_not_bid-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter_single_bidragAndSkjerming-happy.json")));
		stubFor(get("/pen/springapi/sak/sammendrag")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-empty.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertSaksbehandlerHarIkkeTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
		verifyabacDenyPep6dSkipPep2Pep3AndHttpStatusCode(OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep7d() throws IOException, URISyntaxException {
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

	private ResponseEntity<LinkedHashMap> callDokumentOversikBrukerWithAktoerId() throws IOException, URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktBruker/dokumentoversiktbruker_aktoerid.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, LinkedHashMap.class);
	}

	private ResponseEntity<LinkedHashMap> callDokumentOversikBrukerWithFnr() throws IOException, URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktBruker/dokumentoversiktbruker_fnr.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, LinkedHashMap.class);
	}

	private ResponseEntity<LinkedHashMap> callDokumentOversikBrukerWithOrgnr() throws IOException, URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktBruker/dokumentoversiktbruker_orgnr.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, LinkedHashMap.class);
	}

	private Dokumentoversikt getDokumentoversikt(ResponseEntity<LinkedHashMap> responseEntity) {
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		return objectMapper.convertValue(responseEntityData.get("dokumentoversiktBruker"), Dokumentoversikt.class);
	}

	private ResponseEntity<LinkedHashMap> callDokumentOversikBrukerWithFraDato(Map<String, Object> variables) throws IOException, URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktBruker/dokumentoversiktbruker_literals.query"), null, variables);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, LinkedHashMap.class);
	}
}