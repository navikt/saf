package no.nav.saf.endpoints.dokumentoversikt;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.saf.domain.visningsmodell.BrukerTilgangAvvistBegrunnelse;
import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.endpoints.AbstractItest;
import no.nav.saf.endpoints.graphql.GraphQLRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

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
import static org.apache.hc.core5.http.ContentType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class DokumentoversiktFagsakIT extends AbstractItest {

	private static final String KANAL_REFERANSE_ID = "KANAL REFERANSE ID";


	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setup() {
		setupHappyPathAzureToken();
		stubTexasToken();
	}

	@Test
	void shouldHentDokumentoversiktFagsakWithFagsakIdGSAK() throws URISyntaxException {
		abacPermit();

		stubSakMedFagSak("sak/sak-sakerByFagsakIdAndFagsaksystem-happy.json");
		stubFinnjournalposter("finnjournalposter-happy.json");
		stubPdl();

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals("429837329", dokumentoversikt.getJournalposter().get(1).getJournalpostId());
		assertEquals("429812815", dokumentoversikt.getJournalposter().get(2).getJournalpostId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(0).getEksternReferanseId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(1).getEksternReferanseId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(2).getEksternReferanseId());
		assertBrukerTilgang(dokumentoversikt.getJournalposter().toArray(Journalpost[]::new));

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.gsakSakIds", containing("119185782"))));
	}

	@Test
	void shouldHentDokumentoversiktFagsakWithFagsakIdGSAKAndManyVedleggs() throws URISyntaxException {
		abacPermit();

		stubSakMedFagSak("sak/sak-sakerByFagsakIdAndFagsaksystem-happy.json");
		// Merk: i filen under kommer vedleggene i feil rekkefølge "fra dokarkiv" for å teste at saf sorterer dem riktig.
		// i realiteten kommer de alltid i rekkefølge fra dokarkiv, men *kan komme* i uorden internt i saf
		stubFinnjournalposter("finnjournalposter-happy-mangevedlegg.json");
		stubPdl();

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
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
		assertBrukerTilgang(dokumentoversikt.getJournalposter().toArray(Journalpost[]::new));

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.gsakSakIds", containing("119185782"))));
	}

	@Test
	void shouldHentDokumentoversiktFagsakWhenGsakFagsakAndHistoriskFagsakAktoerId() throws URISyntaxException {
		abacPermit();

		stubSakMedFagSak("sak/sak-sakerByFagsakIdAndFagsaksystem-happy.json");
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.withRequestBody(matchingJsonPath("$.gsakSakIds", containing("119185782")))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter-happy.json")));
		stubPdl();

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals("429837329", dokumentoversikt.getJournalposter().get(1).getJournalpostId());
		assertEquals("429812815", dokumentoversikt.getJournalposter().get(2).getJournalpostId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(0).getEksternReferanseId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(1).getEksternReferanseId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(2).getEksternReferanseId());
		assertBrukerTilgang(dokumentoversikt.getJournalposter().toArray(Journalpost[]::new));
	}

	@Test
	void shouldHentDokumentoversiktFagsakWithFagsakIdPSAK() throws URISyntaxException {
		abacPermit();

		setupHappyPathAzureToken();
		stubPensjonBrukerForSak();
		stubFinnjournalposter("finnjournalposter-happy.json");
		stubPdl();
		stubPensjonSakSammendrag("psak-hentSakSammendragListe-happy.json");

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakPsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals("429837329", dokumentoversikt.getJournalposter().get(1).getJournalpostId());
		assertEquals("429812815", dokumentoversikt.getJournalposter().get(2).getJournalpostId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(0).getEksternReferanseId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(1).getEksternReferanseId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(2).getEksternReferanseId());
		assertBrukerTilgang(dokumentoversikt.getJournalposter().toArray(Journalpost[]::new));

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.psakSakIds", containing("21998969"))));
	}

	@Test
	void shouldHentDokumentoversiktFagsakWithFagsakIDSladdet() throws URISyntaxException {
		abacPermit();

		stubSakMedFagSak("sak/sak-sakerByFagsakIdAndFagsaksystem-happy.json");
		stubFinnjournalposter("finnjournalposter_single_sladdet-happy.json");

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(0).getEksternReferanseId());
		assertFalse(dokumentoversikt.getJournalposter().getFirst().isBrukerHarTilgang());
		assertThat(dokumentoversikt.getJournalposter().getFirst().getBrukerTilgangAvvistBegrunnelser()).containsExactly(new BrukerTilgangAvvistBegrunnelse("ikke_avsender_mottaker", null));
	}

	@Test
	void hentSakerTechnicalFail() throws URISyntaxException {
		abacDenyPep1g();
		stubFor(get("/sak?fagsakNr=ARENA-1&applikasjon=AO01")
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals(0, dokumentoversikt.getJournalposter().size());
	}

	@Test
	void hentSakerFunctionalFail() throws URISyntaxException {
		abacDenyPep1g();
		stubFor(get(SAK_API_PATH_MED_QUERY_PARA_FAGSAK_NR)
				.willReturn(aResponse().withStatus(BAD_REQUEST.value())));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals(0, dokumentoversikt.getJournalposter().size());
	}


	@Test
	void shouldHentDokumentoversiktFagsakAktoerTechnicalFail() throws URISyntaxException {
		abacPermit();

		stubPensjonBrukerForSak();
		stubSakMedFagSak("sak/sak-sakerByFagsakIdAndFagsaksystem-happy.json");
		stubFinnjournalposter("finnjournalposter-empty.json");

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals(0, dokumentoversikt.getJournalposter().size());
	}

	@Test
	void shouldHentDokumentoversikHentSakSammendragListeTechnicalFail() throws URISyntaxException {
		abacPermit();

		stubPensjonBrukerForSak();
		stubFinnjournalposter("finnjournalposter-empty.json");
		stubFor(get(PENSJON_API_SAK_SAMMENDRAG_URL)
				.willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("psak-hentSakSammendragListe-technical.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakPsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals(0, dokumentoversikt.getJournalposter().size());
	}

	@Test
	void shouldHentDokumentoversikHentSakSammendragListeFunctionalFail() throws URISyntaxException {
		abacPermit();

		stubPensjonBrukerForSak();
		stubFinnjournalposter("finnjournalposter-empty.json");
		stubFor(get(PENSJON_API_SAK_SAMMENDRAG_URL)
				.willReturn(aResponse().withStatus(BAD_REQUEST.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("psak-hentSakSammendragListe-technical.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakPsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		assertEquals(0, dokumentoversikt.getJournalposter().size());
	}

	@Test
	void shouldReturnEmptyResponseWhenFinnJournalposterEmptyResponse() throws URISyntaxException {
		abacPermit();

		stubSakMedFagSak("sak/sak-sakerByFagsakIdAndFagsaksystem-happy.json");
		stubFinnjournalposter("finnjournalposter-empty.json");
		stubPdl();

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(OK, responseEntity.getStatusCode());
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/pdl")).withRequestBody(matchingJsonPath("$.variables.ident", equalTo("1912374211459"))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"119185782\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[],\"foerste\":5,\"etterPeker\":null}")));
	}

	@Test
	void shouldGetUnauthorizedFromPep1g() throws URISyntaxException {
		abacDenyPep1g();

		stubSakMedFagSak("sak/sak-sakerByFagsakIdAndFagsaksystem-happy.json");
		stubFinnjournalposter("finnjournalposter-happy.json");
		stubPdl();

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(4, dokumentoversikt.getJournalposter().size());
		assertBrukerTilgang(dokumentoversikt.getJournalposter().toArray(Journalpost[]::new));
		assertNull(dokumentoversikt.getSideInfo());
		verifyabacDenyPep1gAndHttpStatusCode(OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep2() throws URISyntaxException {
		abacDenyPep2();
		stubSakMedFagSak("sak/sak-sakerByFagsakIdAndFagsaksystem-FAR-happy.json");
		stubFinnjournalposter("finnjournalposter-empty.json");
		stubPdl();

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[],\"foerste\":5,\"etterPeker\":null}")));
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verifyabacDenyPep2AndHttpStatusCode(OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep2d() throws URISyntaxException {
		abacDenyPep2dSkipPep2();
		stubSakMedFagSak("sak/sak-sakerByFagsakIdAndFagsaksystem-happy.json");
		stubFinnjournalposter("finnjournalposter-dokumentoversiktfagsak-happy.json");
		stubPdl();

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"119185782\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[],\"foerste\":5,\"etterPeker\":null}")));
		assertSaksbehandlerHarIkkeTilgang(dokumentoversikt);
		assertSkjultTittel(dokumentoversikt);
		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
	}

	@Test
	void shouldGetUnauthorizedFromPep3() throws URISyntaxException {
		abacDenyPep3SkipPep2();
		stubSakMedFagSak("sak/sak-sakerBySaksId-happy.json");
		stubFinnjournalposter("finnjournalposter_single_bidragAndSkjerming-happy.json");
		stubPensjonSakSammendrag("psak-hentSakSammendragListe-happy-empty.json");
		stubPdl();
		stubFor(get("/bidrag/201545004").willReturn(aResponse()
				.withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[],\"foerste\":5,\"etterPeker\":null}")));
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verify(2, postRequestedFor(urlEqualTo("/abac")));
		assertEquals(OK, responseEntity.getStatusCode());
		verifyMsGraphMemberOfSeveralGroupsCalled(MS_ID_SAKSBEHANDLER, 1);
	}

	@Test
	void shouldGetUnauthorizedFromPep4() throws URISyntaxException {
		abacDenyPep4SkipPep2OrPep3();
		stubSakMedFagSak("sak/sak-sakerByFagsakIdAndFagsaksystem-FAR-happy.json");
		stubFinnjournalposter("finnjournalposter_single_bidragAndSkjerming-happy.json");
		stubPdl();

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[],\"foerste\":5,\"etterPeker\":null}")));
		verifyabacDenyPep4SkipPep2OrPep3AndHttpStatusCode(OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep5() throws URISyntaxException {
		abacDenyPep5SkipPep2OrPep3();
		stubSakMedFagSak("sak/sak-sakerByFagsakIdAndFagsaksystem-FAR-happy.json");
		stubFinnjournalposter("finnjournalposter_single_bidragAndSkjermingOnlyDokument-happy.json");
		stubPdl();

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertThat(dokumentoversikt.getJournalposter()).isNotEmpty();
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertThat(dokumentoversikt.getJournalposter().get(0).getDokumenter()).isEmpty();
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[],\"foerste\":5,\"etterPeker\":null}")));
		verifyabacDenyPep5SkipPep2OrPep3AndHttpStatusCode(true, OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep6d() throws URISyntaxException {
		abacDenyPep6dSkipPep3OrPep2();

		stubSakMedFagSak("sak/sak-sakerByFagsakIdAndFagsaksystem-FAR-happy.json");
		stubFinnjournalposter("finnjournalposter_single_bidragAndSkjermingOnlyDokvariant-happy.json");
		stubPdl();

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertThat(dokumentoversikt.getJournalposter()).hasSize(1);
		assertFalse(dokumentoversikt.getJournalposter().get(0).isBrukerHarTilgang());
		assertThat(dokumentoversikt.getJournalposter().getFirst().getBrukerTilgangAvvistBegrunnelser()).containsExactly(new BrukerTilgangAvvistBegrunnelse("temaer_unntatt_innsyn", null));
		assertSaksbehandlerHarIkkeTilgang(dokumentoversikt);

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[],\"foerste\":5,\"etterPeker\":null}")));
		verifyabacDenyPep6dSkipPep2AndHttpStatusCode(OK, responseEntity.getStatusCode());
	}

	private ResponseEntity<LinkedHashMap> callDokumentOversikFagsakPsak() throws URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktFagsak/dokumentoversiktfagsak_psak.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, LinkedHashMap.class);
	}

	private ResponseEntity<LinkedHashMap> callDokumentOversikFagsakGsak() throws URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktFagsak/dokumentoversiktfagsak_gsak.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, LinkedHashMap.class);
	}

	private Dokumentoversikt getDokumentoversikt(ResponseEntity<LinkedHashMap> responseEntity) {
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		return objectMapper.convertValue(responseEntityData.get("dokumentoversiktFagsak"), Dokumentoversikt.class);
	}
}