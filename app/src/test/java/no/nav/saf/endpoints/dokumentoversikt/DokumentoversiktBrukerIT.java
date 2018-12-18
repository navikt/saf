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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.saf.endpoints.AbstractItest;
import no.nav.saf.endpoints.GraphQLRequest;
import no.nav.saf.tjeneste.visningsmodell.Dokumentoversikt;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
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

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktBruker/query-aktoerid.json"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));

		ResponseEntity<LinkedHashMap> responseEntity = restTemplate.exchange(requestEntity, LinkedHashMap.class);
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		Dokumentoversikt dokumentoversikt = objectMapper.convertValue(responseEntityData.get("dokumentoversiktBruker"), Dokumentoversikt.class);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals("429837329", dokumentoversikt.getJournalposter().get(1).getJournalpostId());
		assertEquals("429812815", dokumentoversikt.getJournalposter().get(2).getJournalpostId());

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

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktBruker/query-fnr.json"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));

		ResponseEntity<LinkedHashMap> responseEntity = restTemplate.exchange(requestEntity, LinkedHashMap.class);
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		Dokumentoversikt dokumentoversikt = objectMapper.convertValue(responseEntityData.get("dokumentoversiktBruker"), Dokumentoversikt.class);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals("429837329", dokumentoversikt.getJournalposter().get(1).getJournalpostId());
		assertEquals("429812815", dokumentoversikt.getJournalposter().get(2).getJournalpostId());

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

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktBruker/query-orgnr.json"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));

		ResponseEntity<LinkedHashMap> responseEntity = restTemplate.exchange(requestEntity, LinkedHashMap.class);
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		Dokumentoversikt dokumentoversikt = objectMapper.convertValue(responseEntityData.get("dokumentoversiktBruker"), Dokumentoversikt.class);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals("429837329", dokumentoversikt.getJournalposter().get(1).getJournalpostId());
		assertEquals("429812815", dokumentoversikt.getJournalposter().get(2).getJournalpostId());

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.gsakSakIds", containing("135695442"))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.gsakSakIds", containing("135695449"))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.gsakSakIds", containing("135695448"))));

		verify(0, postRequestedFor(urlEqualTo("/aktoerv2")));
		verify(0, postRequestedFor(urlEqualTo("/servicegw")));
	}

	@Test
	public void hentIdentForAktoerIdTechnicalFail() throws IOException, URISyntaxException {
		abacDeny();
		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerId-technical-fail.xml")));

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktBruker/query-aktoerid.json"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));

		ResponseEntity<LinkedHashMap> responseEntity = restTemplate.exchange(requestEntity, LinkedHashMap.class);
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		Dokumentoversikt dokumentoversikt = objectMapper.convertValue(responseEntityData.get("dokumentoversiktBruker"), Dokumentoversikt.class);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode()); //??

		verify(postRequestedFor(urlEqualTo("/aktoerv2")).withRequestBody(matchingXPath("//aktoerId/text()", equalTo(AKTOER_ID))));
		verify(0, postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")));
		verify(0, postRequestedFor(urlEqualTo("/servicegw")));
	}

	@Test
	public void hentIdentForAktoerIdFunctionalFail() throws IOException, URISyntaxException {
		abacDeny();
		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerId-functional-fail.xml")));
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktBruker/query-aktoerid.json"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));

		ResponseEntity<LinkedHashMap> responseEntity = restTemplate.exchange(requestEntity, LinkedHashMap.class);
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		Dokumentoversikt dokumentoversikt = objectMapper.convertValue(responseEntityData.get("dokumentoversiktBruker"), Dokumentoversikt.class);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode()); //??

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

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktBruker/query-aktoerid.json"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));

		ResponseEntity<LinkedHashMap> responseEntity = restTemplate.exchange(requestEntity, LinkedHashMap.class);
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		Dokumentoversikt dokumentoversikt = objectMapper.convertValue(responseEntityData.get("dokumentoversiktBruker"), Dokumentoversikt.class);

		assertTrue(dokumentoversikt.getJournalposter().isEmpty());
		assertEquals(null, dokumentoversikt.getSideInfo());

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

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktBruker/query-aktoerid.json"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));

		ResponseEntity<LinkedHashMap> responseEntity = restTemplate.exchange(requestEntity, LinkedHashMap.class);
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		Dokumentoversikt dokumentoversikt = objectMapper.convertValue(responseEntityData.get("dokumentoversiktBruker"), Dokumentoversikt.class);

		assertTrue(dokumentoversikt.getJournalposter().isEmpty());
		assertEquals(null, dokumentoversikt.getSideInfo());

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		verify(postRequestedFor(urlEqualTo("/aktoerv2")).withRequestBody(matchingXPath("//aktoerId/text()", equalTo(AKTOER_ID))));
		verify(1, postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")));
		verify(postRequestedFor(urlEqualTo("/servicegw")).withRequestBody(matchingXPath("//personident/text()", equalTo("***gammelt_fnr***"))));
	}

//  todo implement
//	@Test
//	@Disabled
//	public void finnjournalposterFail() throws IOException, URISyntaxException {
//	}

// todo implement
//	@Test
//	@Disabled
//	public void serviewgwFail() {
//	}

	// todo test for om dokumentoversikt filtrerer mhp abac?

	// todo vurder arametrized test for gsak, for å teste mhp aktoerid

}