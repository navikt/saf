package no.nav.saf.endpoints.dokumentoversikt;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
public class DokumentoversiktFagsakIT extends AbstractItest {

	private static final String FAGSAK_ID = "***gammelt_fnr***59";
	private static final String FAGSAK_SYSTEM = "AO01";

	private ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void shouldHentDokumentoversiktFagsakWithFagsakIdGSAK() throws IOException, URISyntaxException {
		abacPermit();

		stubFor(get("/gsak?fagsakNr=" + FAGSAK_ID + "&applikasjon=" + FAGSAK_SYSTEM)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("gsak/gsak-sakerByFagsakIdAndFagsaksystem-happy.json")));

		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())

						.withBodyFile("joark/finnjournalposter-happy.json")));
		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerIdListe-happy.xml")));


		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktFagsak/query-fagsakid-gsak.json"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));

		ResponseEntity<LinkedHashMap> responseEntity = restTemplate.exchange(requestEntity, LinkedHashMap.class);
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		Dokumentoversikt dokumentoversikt = objectMapper.convertValue(responseEntityData.get("dokumentoversiktFagsak"), Dokumentoversikt.class);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals("429837329", dokumentoversikt.getJournalposter().get(1).getJournalpostId());
		assertEquals("429812815", dokumentoversikt.getJournalposter().get(2).getJournalpostId());

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.gsakSakIds", containing("119185782"))));
	}

	@Test
	public void shouldHentDokumentoversiktFagsakWithFagsakIdPSAK() throws IOException, URISyntaxException {
		abacPermit();

		stubFor(get("/pensjonsakrs")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("psak/psak-hentBrukerForSak-happy.json")));

		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter-happy.json")));

		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentAktoerIdForIdent-happy.xml")));

		stubFor(post("/servicegw")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy.xml")));

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktFagsak/query-fagsakid-psak.json"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));

		ResponseEntity<LinkedHashMap> responseEntity = restTemplate.exchange(requestEntity, LinkedHashMap.class);
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		Dokumentoversikt dokumentoversikt = objectMapper.convertValue(responseEntityData.get("dokumentoversiktFagsak"), Dokumentoversikt.class);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals("429837329", dokumentoversikt.getJournalposter().get(1).getJournalpostId());
		assertEquals("429812815", dokumentoversikt.getJournalposter().get(2).getJournalpostId());

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.psakSakIds", containing("21998969"))));
	}

	@Test
	public void hentSakerTechnicalFail() throws IOException, URISyntaxException {
		abacDeny();
		stubFor(get("/gsak?fagsakNr=***gammelt_fnr***59&applikasjon=AO01")
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktFagsak/query-fagsakid-gsak.json"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));

		ResponseEntity<LinkedHashMap> responseEntity = restTemplate.exchange(requestEntity, LinkedHashMap.class);
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		Dokumentoversikt dokumentoversikt = objectMapper.convertValue(responseEntityData.get("dokumentoversiktFagsak"), Dokumentoversikt.class);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(0, dokumentoversikt.getJournalposter().size());
	}

	@Test
	public void hentSakerFunctionalFail() throws IOException, URISyntaxException {
		abacDeny();
		stubFor(get("/gsak?fagsakNr=***gammelt_fnr***59&applikasjon=AO01")
				.willReturn(aResponse().withStatus(HttpStatus.BAD_REQUEST.value())));

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktFagsak/query-fagsakid-gsak.json"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));

		ResponseEntity<LinkedHashMap> responseEntity = restTemplate.exchange(requestEntity, LinkedHashMap.class);
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		Dokumentoversikt dokumentoversikt = objectMapper.convertValue(responseEntityData.get("dokumentoversiktFagsak"), Dokumentoversikt.class);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(0, dokumentoversikt.getJournalposter().size());
	}


	@Test
	public void shouldHentDokumentoversiktFagsakAktoerTechnicalFail() throws IOException, URISyntaxException {
		abacPermit();

		stubFor(get("/pensjonsakrs")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("psak/psak-hentBrukerForSak-happy.json")));

		stubFor(get("/gsak?fagsakNr=" + FAGSAK_ID + "&applikasjon=" + FAGSAK_SYSTEM)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("gsak/gsak-sakerByFagsakIdAndFagsaksystem-happy.json")));

		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter-empty.json")));

		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerIdListe-technical.xml")));


		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktFagsak/query-fagsakid-gsak.json"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));

		ResponseEntity<LinkedHashMap> responseEntity = restTemplate.exchange(requestEntity, LinkedHashMap.class);
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		Dokumentoversikt dokumentoversikt = objectMapper.convertValue(responseEntityData.get("dokumentoversiktFagsak"), Dokumentoversikt.class);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(0, dokumentoversikt.getJournalposter().size());
	}

	@Test
	public void shouldHentDokumentoversikHentSakSammendragListeTechnicalFail() throws IOException, URISyntaxException {
		abacPermit();

		stubFor(get("/pensjonsakrs")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("psak/psak-hentBrukerForSak-happy.json")));

		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter-empty.json")));

		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentAktoerIdForIdent-happy.xml")));

		stubFor(post("/servicegw")
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
						.withBodyFile("psak-hentSakSammendragListe-technical.xml")));

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktFagsak/query-fagsakid-psak.json"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));

		ResponseEntity<LinkedHashMap> responseEntity = restTemplate.exchange(requestEntity, LinkedHashMap.class);
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		Dokumentoversikt dokumentoversikt = objectMapper.convertValue(responseEntityData.get("dokumentoversiktFagsak"), Dokumentoversikt.class);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(0, dokumentoversikt.getJournalposter().size());
	}

	@Test
	public void shouldHentDokumentoversikHentSakSammendragListeFunctionalFail() throws IOException, URISyntaxException {
		abacPermit();

		stubFor(get("/pensjonsakrs")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("psak/psak-hentBrukerForSak-happy.json")));

		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter-empty.json")));

		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentAktoerIdForIdent-happy.xml")));

		stubFor(post("/servicegw")
				.willReturn(aResponse().withStatus(HttpStatus.BAD_REQUEST.value())
						.withBodyFile("psak-hentSakSammendragListe-technical.xml")));

		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktFagsak/query-fagsakid-psak.json"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));

		ResponseEntity<LinkedHashMap> responseEntity = restTemplate.exchange(requestEntity, LinkedHashMap.class);
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		Dokumentoversikt dokumentoversikt = objectMapper.convertValue(responseEntityData.get("dokumentoversiktFagsak"), Dokumentoversikt.class);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(0, dokumentoversikt.getJournalposter().size());
	}
}