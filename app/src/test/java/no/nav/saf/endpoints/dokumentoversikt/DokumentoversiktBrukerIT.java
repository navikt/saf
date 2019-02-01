package no.nav.saf.endpoints.dokumentoversikt;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingXPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.endpoints.AbstractItest;
import no.nav.saf.endpoints.GraphQLRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class DokumentoversiktBrukerIT extends AbstractItest {

	private static final String AKTOER_ID = "***gammelt_fnr***59";
	private static final String FNR = "***gammelt_fnr***";
	private static final String ORG_NR = "201545004";

	private ObjectMapper objectMapper = new ObjectMapper();


	@Autowired
	private ApplicationContext applicationContext;

	@Test
	public void shouldHentDokumentoversiktBrukerWithAktoerID() throws IOException, URISyntaxException {
		abacPermit();
		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerId-happy.xml")));
		stubFor(post("/sts")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("sts/sts-happy.xml")));
		stubFor(get("/gsak?aktoerId=" + AKTOER_ID)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("gsak/gsak-sakerBySaksId-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter-happy.json")));
		stubFor(post("/servicegw")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy.xml")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals("429837329", dokumentoversikt.getJournalposter().get(1).getJournalpostId());
		assertEquals("429812815", dokumentoversikt.getJournalposter().get(2).getJournalpostId());
		assertSaksbehanlerHarTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/aktoerv2")).withRequestBody(matchingXPath("//aktoerId/text()", equalTo(AKTOER_ID))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.gsakSakIds", containing("135695442"))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.gsakSakIds", containing("135695449"))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.gsakSakIds", containing("135695448"))));
		verify(postRequestedFor(urlEqualTo("/servicegw")).withRequestBody(matchingXPath("//personident/text()", equalTo("***gammelt_fnr***"))));
	}

	@Test
	public void shouldHentDokumentoversiktBrukerWithFNR() throws IOException, URISyntaxException {
		abacPermit();
		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentIdentForFNR-happy.xml")));
		stubFor(post("/sts")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("sts/sts-happy.xml")));
		stubFor(get("/gsak?aktoerId=" + AKTOER_ID)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("gsak/gsak-sakerBySaksId-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter-happy.json")));
		stubFor(post("/servicegw")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy.xml")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithFnr();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals("429837329", dokumentoversikt.getJournalposter().get(1).getJournalpostId());
		assertEquals("429812815", dokumentoversikt.getJournalposter().get(2).getJournalpostId());
		assertSaksbehanlerHarTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/aktoerv2")).withRequestBody(matchingXPath("//ident/text()", equalTo(FNR))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.gsakSakIds", containing("135695442"))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.gsakSakIds", containing("135695449"))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.gsakSakIds", containing("135695448"))));
		verify(postRequestedFor(urlEqualTo("/servicegw")).withRequestBody(matchingXPath("//personident/text()", equalTo("***gammelt_fnr***"))));
	}


	@Test
	public void shouldHentDokumentoversiktBrukerWithOrgNr() throws IOException, URISyntaxException {
		abacPermit();
		stubFor(post("/sts")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("sts/sts-happy.xml")));
		stubFor(get("/gsak?orgnr=" + ORG_NR)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("gsak/gsak-sakerBySaksId-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithOrgnr();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals("429837329", dokumentoversikt.getJournalposter().get(1).getJournalpostId());
		assertEquals("429812815", dokumentoversikt.getJournalposter().get(2).getJournalpostId());
		assertSaksbehanlerHarTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.gsakSakIds", containing("135695442"))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.gsakSakIds", containing("135695449"))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.gsakSakIds", containing("135695448"))));
		verify(0, postRequestedFor(urlEqualTo("/aktoerv2")));
		verify(0, postRequestedFor(urlEqualTo("/servicegw")));
	}

	@Test
	public void hentIdentForAktoerIdTechnicalFail() throws IOException, URISyntaxException {
		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerId-technical-fail.xml")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/aktoerv2")).withRequestBody(matchingXPath("//aktoerId/text()", equalTo(AKTOER_ID))));
		verify(0, postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")));
		verify(0, postRequestedFor(urlEqualTo("/servicegw")));
	}

	@Test
	public void hentIdentForAktoerIdFunctionalFail() throws IOException, URISyntaxException {
		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerId-functional-fail.xml")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/aktoerv2")).withRequestBody(matchingXPath("//aktoerId/text()", equalTo(AKTOER_ID))));
		verify(0, postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")));
		verify(0, postRequestedFor(urlEqualTo("/servicegw")));
	}

	@Test
	public void HentSakerByAktoerIdGsakTechnicalFail() throws IOException, URISyntaxException {
		abacPermit();
		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerId-happy.xml")));
		stubFor(post("/sts")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("sts/sts-happy.xml")));
		stubFor(get("/gsak?aktoerId=" + AKTOER_ID)
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter-empty.json")));
		stubFor(post("/servicegw")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy.xml")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		verify(postRequestedFor(urlEqualTo("/aktoerv2")).withRequestBody(matchingXPath("//aktoerId/text()", equalTo(AKTOER_ID))));
		verify(1, postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")));
		verify(postRequestedFor(urlEqualTo("/servicegw")).withRequestBody(matchingXPath("//personident/text()", equalTo("***gammelt_fnr***"))));
	}

	@Test
	public void HentSakerByAktoerIdGsakFunctionalFail() throws IOException, URISyntaxException {
		abacPermit();
		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerId-happy.xml")));
		stubFor(post("/sts")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("sts/sts-happy.xml")));
		stubFor(get("/gsak?aktoerId=" + AKTOER_ID)
				.willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter-empty.json")));
		stubFor(post("/servicegw")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy.xml")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		verify(postRequestedFor(urlEqualTo("/aktoerv2")).withRequestBody(matchingXPath("//aktoerId/text()", equalTo(AKTOER_ID))));
		verify(1, postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")));
		verify(postRequestedFor(urlEqualTo("/servicegw")).withRequestBody(matchingXPath("//personident/text()", equalTo("***gammelt_fnr***"))));
	}

	@Test
	public void finnjournalposterFail() throws IOException, URISyntaxException {
		abacPermit();
		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerId-happy.xml")));
		stubFor(post("/sts")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("sts/sts-happy.xml")));
		stubFor(get("/gsak?aktoerId=" + AKTOER_ID)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("gsak/gsak-sakerBySaksId-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter-empty.json")));
		stubFor(post("/servicegw")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy.xml")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/aktoerv2")).withRequestBody(matchingXPath("//aktoerId/text()", equalTo(AKTOER_ID))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.gsakSakIds", containing("135695442"))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.gsakSakIds", containing("135695449"))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.gsakSakIds", containing("135695448"))));
		verify(postRequestedFor(urlEqualTo("/servicegw")).withRequestBody(matchingXPath("//personident/text()", equalTo("***gammelt_fnr***"))));
	}

	@Test
	public void shouldGetUnauthorizedFromPep1g() throws IOException, URISyntaxException {
		abacDenyPep1g();
		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerId-happy.xml")));
		stubFor(post("/sts")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("sts/sts-happy.xml")));
		stubFor(get("/gsak?aktoerId=" + AKTOER_ID)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("gsak/gsak-sakerBySaksId-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter-happy.json")));
		stubFor(post("/servicegw")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy.xml")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verifyabacDenyPep1gAndHttpStatusCode(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	public void shouldGetUnauthorizedFromPep2() throws IOException, URISyntaxException {
		abacDenyPep2();
		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerId-happy.xml")));
		stubFor(post("/sts")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("sts/sts-happy.xml")));
		stubFor(get("/gsak?aktoerId=" + AKTOER_ID)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("gsak/gsak-sakerBySaksId-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter_single_bidragAndSkjerming-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verifyabacDenyPep2AndHttpStatusCode(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	public void shouldGetUnauthorizedFromPep2d() throws IOException, URISyntaxException {
		abacDenyPep2dDokumentOversikt();
		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerId-happy.xml")));
		stubFor(post("/sts")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("sts/sts-happy.xml")));
		stubFor(get("/gsak?aktoerId=" + AKTOER_ID)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("gsak/gsak-sakerBySaksId-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter-happy.json")));
		stubFor(post("/servicegw")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy.xml")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verifyabacDenyPep2dAndHttpStatusCode(true, HttpStatus.OK, responseEntity.getStatusCode());
	}

	private ResponseEntity<LinkedHashMap> callDokumentOversikBrukerWithAktoerId() throws IOException, URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktBruker/query-aktoerid.json"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, LinkedHashMap.class);
	}

	private ResponseEntity<LinkedHashMap> callDokumentOversikBrukerWithFnr() throws IOException, URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktBruker/query-fnr.json"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, LinkedHashMap.class);
	}

	private ResponseEntity<LinkedHashMap> callDokumentOversikBrukerWithOrgnr() throws IOException, URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktBruker/query-orgnr.json"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, LinkedHashMap.class);
	}

	private Dokumentoversikt getDokumentoversikt(ResponseEntity<LinkedHashMap> responseEntity) {
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		return objectMapper.convertValue(responseEntityData.get("dokumentoversiktBruker"), Dokumentoversikt.class);
	}
}