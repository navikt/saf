package no.nav.saf.endpoints.dokumentoversikt;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.endpoints.AbstractItest;
import no.nav.saf.endpoints.GraphQLRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
class DokumentoversiktFagsakIT extends AbstractItest {

	private static final String FAGSAK_ID = "ARENA-1";
	private static final String FAGSAK_SYSTEM = "AO01";
	private static final String KANAL_REFERANSE_ID = "KANAL REFERANSE ID";

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldHentDokumentoversiktFagsakWithFagsakIdGSAK() throws IOException, URISyntaxException {
		abacPermit();

		stubFor(get("/gsak?fagsakNr=" + FAGSAK_ID + "&applikasjon=" + FAGSAK_SYSTEM)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakerByFagsakIdAndFagsaksystem-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter-happy.json")));
		stubFor(post("/reststs")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("sts/sts-token.json")));
		stubFor(post("/pdl")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/hentPdlDataForIdent-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals("429837329", dokumentoversikt.getJournalposter().get(1).getJournalpostId());
		assertEquals("429812815", dokumentoversikt.getJournalposter().get(2).getJournalpostId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(0).getEksternReferanseId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(1).getEksternReferanseId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(2).getEksternReferanseId());

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.gsakSakIds", containing("119185782"))));
	}

	@Test
	void shouldHentDokumentoversiktFagsakWithFagsakIdPSAK() throws IOException, URISyntaxException {
		abacPermit();

		stubFor(get("/pensjonsakrs")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("psak/psak-hentBrukerForSak-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter-happy.json")));
		stubFor(post("/reststs")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("sts/sts-token.json")));
		stubFor(post("/pdl")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/hentPdlDataForIdent-happy.json")));
		stubFor(post("/pensjonsakv1")
				.willReturn(aResponse().withStatus(OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy.xml")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakPsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals("429837329", dokumentoversikt.getJournalposter().get(1).getJournalpostId());
		assertEquals("429812815", dokumentoversikt.getJournalposter().get(2).getJournalpostId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(0).getEksternReferanseId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(1).getEksternReferanseId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(2).getEksternReferanseId());

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.psakSakIds", containing("21998969"))));
	}

	@Test
	void shouldHentDokumentoversiktFagsakWithFagsakIDSladdet() throws IOException, URISyntaxException {
		abacPermit();

		stubFor(get("/gsak?fagsakNr=" + FAGSAK_ID + "&applikasjon=" + FAGSAK_SYSTEM)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakerByFagsakIdAndFagsaksystem-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter_single_sladdet-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(0).getEksternReferanseId());
	}

	@Test
	void hentSakerTechnicalFail() throws IOException, URISyntaxException {
		abacDenyPep1g();
		stubFor(get("/gsak?fagsakNr=ARENA-1&applikasjon=AO01")
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals(0, dokumentoversikt.getJournalposter().size());
	}

	@Test
	void hentSakerFunctionalFail() throws IOException, URISyntaxException {
		abacDenyPep1g();
		stubFor(get("/gsak?fagsakNr=ARENA-1&applikasjon=AO01")
				.willReturn(aResponse().withStatus(BAD_REQUEST.value())));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals(0, dokumentoversikt.getJournalposter().size());
	}


	@Test
	void shouldHentDokumentoversiktFagsakAktoerTechnicalFail() throws IOException, URISyntaxException {
		abacPermit();

		stubFor(get("/pensjonsakrs")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("psak/psak-hentBrukerForSak-happy.json")));
		stubFor(get("/gsak?fagsakNr=" + FAGSAK_ID + "&applikasjon=" + FAGSAK_SYSTEM)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakerByFagsakIdAndFagsaksystem-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter-empty.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals(0, dokumentoversikt.getJournalposter().size());
	}

	@Test
	void shouldHentDokumentoversikHentSakSammendragListeTechnicalFail() throws IOException, URISyntaxException {
		abacPermit();

		stubFor(get("/pensjonsakrs")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("psak/psak-hentBrukerForSak-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter-empty.json")));
		stubFor(post("/pensjonsakv1")
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())
						.withBodyFile("psak-hentSakSammendragListe-technical.xml")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakPsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals(0, dokumentoversikt.getJournalposter().size());
	}

	@Test
	void shouldHentDokumentoversikHentSakSammendragListeFunctionalFail() throws IOException, URISyntaxException {
		abacPermit();

		stubFor(get("/pensjonsakrs")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("psak/psak-hentBrukerForSak-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter-empty.json")));
		stubFor(post("/pensjonsakv1")
				.willReturn(aResponse().withStatus(BAD_REQUEST.value())
						.withBodyFile("psak-hentSakSammendragListe-technical.xml")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakPsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals(0, dokumentoversikt.getJournalposter().size());
	}

	@Test
	void shouldReturnEmptyResponseWhenFinnJournalposterEmptyResponse() throws IOException, URISyntaxException {
		abacPermit();

		stubFor(get("/gsak?fagsakNr=" + FAGSAK_ID + "&applikasjon=" + FAGSAK_SYSTEM)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakerByFagsakIdAndFagsaksystem-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter-empty.json")));
		stubFor(post("/reststs")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("sts/sts-token.json")));
		stubFor(post("/pdl")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/hentPdlDataForIdent-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/pdl")).withRequestBody(matchingJsonPath("$.variables.ident", equalTo("1912374211459"))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"119185782\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[],\"foerste\":5,\"etterPeker\":null}")));
	}

	@Test
	void shouldGetUnauthorizedFromPep1g() throws IOException, URISyntaxException {
		abacDenyPep1g();

		stubFor(get("/gsak?fagsakNr=" + FAGSAK_ID + "&applikasjon=" + FAGSAK_SYSTEM)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakerByFagsakIdAndFagsaksystem-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter-happy.json")));
		stubFor(post("/reststs")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("sts/sts-token.json")));
		stubFor(post("/pdl")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/hentPdlDataForIdent-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(3, dokumentoversikt.getJournalposter().size());
		assertNull(dokumentoversikt.getSideInfo());
		verifyabacDenyPep1gAndHttpStatusCode(OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep2() throws IOException, URISyntaxException {
		abacDenyPep2();
		stubFor(get("/gsak?fagsakNr=" + FAGSAK_ID + "&applikasjon=" + FAGSAK_SYSTEM)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakerByFagsakIdAndFagsaksystem-FAR-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter-empty.json")));
		stubFor(post("/reststs")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("sts/sts-token.json")));
		stubFor(post("/pdl")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/hentPdlDataForIdent-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[],\"foerste\":5,\"etterPeker\":null}")));
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verifyabacDenyPep2AndHttpStatusCode(OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep2d() throws IOException, URISyntaxException {
		abacDenyPep2d();
		stubFor(get("/gsak?fagsakNr=" + FAGSAK_ID + "&applikasjon=" + FAGSAK_SYSTEM)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakerByFagsakIdAndFagsaksystem-FAR-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter_single_bidragAndSkjerming-happy.json")));
		stubFor(post("/reststs")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("sts/sts-token.json")));
		stubFor(post("/pdl")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/hentPdlDataForIdent-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"112233445\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[],\"foerste\":5,\"etterPeker\":null}")));
		assertSaksbehandlerHarTilgang(dokumentoversikt);
		verifyabacDenyPep2dAndHttpStatusCode(true, OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep3() throws IOException, URISyntaxException {
		abacDenyPep3SkipPep2();
		stubFor(get("/gsak?fagsakNr=" + FAGSAK_ID + "&applikasjon=" + FAGSAK_SYSTEM)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakerBySaksId-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter_single_bidragAndSkjerming-happy.json")));
		stubFor(post("/reststs")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("sts/sts-token.json")));
		stubFor(post("/pdl")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/hentPdlDataForIdent-happy.json")));
		stubFor(get("/bidrag/201545004").willReturn(aResponse()
				.withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[],\"foerste\":5,\"etterPeker\":null}")));
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verifyabacDenyPep3ASkipPep2AndHttpStatusCode(OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep4() throws IOException, URISyntaxException {
		abacDenyPep4SkipPep2OrPep3();
		stubFor(get("/gsak?fagsakNr=" + FAGSAK_ID + "&applikasjon=" + FAGSAK_SYSTEM)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakerByFagsakIdAndFagsaksystem-FAR-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter_single_bidragAndSkjerming-happy.json")));
		stubFor(post("/reststs")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("sts/sts-token.json")));
		stubFor(post("/pdl")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/hentPdlDataForIdent-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"112233445\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[],\"foerste\":5,\"etterPeker\":null}")));
		verifyabacDenyPep4SkipPep2OrPep3AndHttpStatusCode(OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep5() throws IOException, URISyntaxException {
		abacDenyPep5SkipPep2OrPep3();
		stubFor(get("/gsak?fagsakNr=" + FAGSAK_ID + "&applikasjon=" + FAGSAK_SYSTEM)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakerByFagsakIdAndFagsaksystem-FAR-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter_single_bidragAndSkjerming-happy.json")));
		stubFor(post("/reststs")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("sts/sts-token.json")));
		stubFor(post("/pdl")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/hentPdlDataForIdent-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertFalse(dokumentoversikt.getJournalposter().isEmpty());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertTrue(dokumentoversikt.getJournalposter().get(0).getDokumenter().isEmpty());
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"112233445\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[],\"foerste\":5,\"etterPeker\":null}")));
		verifyabacDenyPep5SkipPep2OrPep3AndHttpStatusCode(true, OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep6d() throws IOException, URISyntaxException {
		abacDenyPep6dSkipPep3OrPep2();

		stubFor(get("/gsak?fagsakNr=" + FAGSAK_ID + "&applikasjon=" + FAGSAK_SYSTEM)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakerByFagsakIdAndFagsaksystem-FAR-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter_single_bidragAndSkjerming-happy.json")));
		stubFor(post("/reststs")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("sts/sts-token.json")));
		stubFor(post("/pdl")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/hentPdlDataForIdent-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertSaksbehandlerHarIkkeTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"112233445\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[],\"foerste\":5,\"etterPeker\":null}")));
		verifyabacDenyPep6dSkipPep2AndHttpStatusCode(OK, responseEntity.getStatusCode());
	}

	private ResponseEntity<LinkedHashMap> callDokumentOversikFagsakPsak() throws IOException, URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktFagsak/dokumentoversiktfagsak_psak.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, LinkedHashMap.class);
	}

	private ResponseEntity<LinkedHashMap> callDokumentOversikFagsakGsak() throws IOException, URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktFagsak/dokumentoversiktfagsak_gsak.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, LinkedHashMap.class);
	}

	private Dokumentoversikt getDokumentoversikt(ResponseEntity<LinkedHashMap> responseEntity) {
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		return objectMapper.convertValue(responseEntityData.get("dokumentoversiktFagsak"), Dokumentoversikt.class);
	}
}