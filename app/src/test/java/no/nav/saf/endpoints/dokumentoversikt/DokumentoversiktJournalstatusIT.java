package no.nav.saf.endpoints.dokumentoversikt;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.endpoints.AbstractItest;
import no.nav.saf.endpoints.GraphQLRequest;
import org.apache.http.HttpHeaders;
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author Erik Bråten, Visma Consulting.
 */
class DokumentoversiktJournalstatusIT extends AbstractItest {

	private static final String KANAL_REFERANSE_ID = "KANAL REFERANSE ID";

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldHentDokumentoversiktJournalstatusUkjentBruker() throws IOException, URISyntaxException {
		abacPermit();

		stubFor(post("/hentjournalsakinfo/finnjournalposterstatus")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposterstatus-ukjent_bruker.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversiktJournalstatusUkjentBruker();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(4, dokumentoversikt.getJournalposter().size()); // 1 feilregistrert journalpost filterers bort
		assertEquals("453221424", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals(Journalstatus.UKJENT_BRUKER, dokumentoversikt.getJournalposter().get(0).getJournalstatus());
		assertEquals("453211096", dokumentoversikt.getJournalposter().get(1).getJournalpostId());
		assertEquals(Journalstatus.UKJENT_BRUKER, dokumentoversikt.getJournalposter().get(1).getJournalstatus());
		assertEquals("452943905", dokumentoversikt.getJournalposter().get(2).getJournalpostId());
		assertEquals(Journalstatus.UKJENT_BRUKER, dokumentoversikt.getJournalposter().get(2).getJournalstatus());
		assertEquals("452929051", dokumentoversikt.getJournalposter().get(3).getJournalpostId());
		assertEquals(Journalstatus.UKJENT_BRUKER, dokumentoversikt.getJournalposter().get(3).getJournalstatus());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(0).getEksternReferanseId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(1).getEksternReferanseId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(2).getEksternReferanseId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(3).getEksternReferanseId());

		assertEquals(base64("452929051"), dokumentoversikt.getSideInfo().getSluttpeker());
		assertTrue(dokumentoversikt.getSideInfo().isFinnesNesteSide());

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposterstatus"))
				.withRequestBody(matchingJsonPath("$.journalstatus", containing("UB"))));
		verify(3, postRequestedFor(urlEqualTo("/abac"))); // kun journalpost 452929051 med skjerming sjekkes mot abac
	}

	@Test
	void shouldHentDokumentoversiktJournalstatusUtgaar() throws IOException, URISyntaxException {
		abacPermit();

		stubFor(post("/hentjournalsakinfo/finnjournalposterstatus")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposterstatus-utgaar.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversiktJournalstatusUtgaar();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(5, dokumentoversikt.getJournalposter().size());
		assertEquals("453466679", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals(Journalstatus.UTGAAR, dokumentoversikt.getJournalposter().get(0).getJournalstatus());
		assertEquals("453465088", dokumentoversikt.getJournalposter().get(1).getJournalpostId());
		assertEquals(Journalstatus.UTGAAR, dokumentoversikt.getJournalposter().get(1).getJournalstatus());
		assertEquals("453438556", dokumentoversikt.getJournalposter().get(2).getJournalpostId());
		assertEquals(Journalstatus.UTGAAR, dokumentoversikt.getJournalposter().get(2).getJournalstatus());
		assertEquals("453414874", dokumentoversikt.getJournalposter().get(3).getJournalpostId());
		assertEquals(Journalstatus.UTGAAR, dokumentoversikt.getJournalposter().get(3).getJournalstatus());
		assertEquals("453375495", dokumentoversikt.getJournalposter().get(4).getJournalpostId());
		assertEquals(Journalstatus.UTGAAR, dokumentoversikt.getJournalposter().get(4).getJournalstatus());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(0).getEksternReferanseId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(1).getEksternReferanseId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(2).getEksternReferanseId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(3).getEksternReferanseId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(4).getEksternReferanseId());

		assertEquals(base64("453375495"), dokumentoversikt.getSideInfo().getSluttpeker());
		assertTrue(dokumentoversikt.getSideInfo().isFinnesNesteSide());

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposterstatus"))
				.withRequestBody(matchingJsonPath("$.journalstatus", containing("U"))));
		verify(5, postRequestedFor(urlEqualTo("/abac"))); // ingen skjerming så kun pep4 sjekkes
	}

	@Test
	void finnJournalposterStatusTechnicalException() throws IOException, URISyntaxException {
		abacPermit();

		stubFor(post("/hentjournalsakinfo/finnjournalposterstatus")
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposterstatus-technical.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversiktJournalstatusUtgaar();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		verifyEmptyJournalpostListeAndEmptySideInfo(dokumentoversikt);
	}

	@Test
	void finnJournalposterStatusFunctionalException() throws IOException, URISyntaxException {
		abacPermit();

		stubFor(post("/hentjournalsakinfo/finnjournalposterstatus")
				.willReturn(aResponse().withStatus(HttpStatus.BAD_REQUEST.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposterstatus-functional.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversiktJournalstatusUtgaar();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		verifyEmptyJournalpostListeAndEmptySideInfo(dokumentoversikt);
	}

	@Test
	void finnJournalposterStatusEmptyResponse() throws IOException, URISyntaxException {
		abacPermit();

		stubFor(post("/hentjournalsakinfo/finnjournalposterstatus")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter-empty.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversiktJournalstatusUtgaar();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		verifyEmptyJournalpostListeAndEmptySideInfo(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposterstatus"))
				.withRequestBody(containing("{\"journalstatus\":\"U\",\"fraDato\":\"2019-01-01\",\"journalposttyper\":[\"I\",\"U\",\"N\"],\"foerste\":5,\"etterPeker\":null}")));
	}

	@Test
	void shouldGetAuthorized() throws IOException, URISyntaxException {
		abacPermit();

		stubFor(post("/hentjournalsakinfo/finnjournalposterstatus")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposterstatus_single-utgaar.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversiktJournalstatusUtgaar();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(1, dokumentoversikt.getJournalposter().size());
		assertEquals("453466679", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals(1, dokumentoversikt.getJournalposter().get(0).getDokumenter().size());

		assertEquals(base64("453466679"), dokumentoversikt.getSideInfo().getSluttpeker());
		assertFalse(dokumentoversikt.getSideInfo().isFinnesNesteSide());

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposterstatus"))
				.withRequestBody(containing("{\"journalstatus\":\"U\",\"fraDato\":\"2019-01-01\",\"journalposttyper\":[\"I\",\"U\",\"N\"],\"foerste\":5,\"etterPeker\":null}")));
		verify(3, postRequestedFor(urlEqualTo("/abac")));
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep4() throws IOException, URISyntaxException {
		abacDenyPep4SkipPep1gPep2Pep2dPep3();

		stubFor(post("/hentjournalsakinfo/finnjournalposterstatus")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposterstatus_single-utgaar.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversiktJournalstatusUtgaar();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(0, dokumentoversikt.getJournalposter().size());
		assertEquals(base64("453466679"), dokumentoversikt.getSideInfo().getSluttpeker());
		assertFalse(dokumentoversikt.getSideInfo().isFinnesNesteSide());
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposterstatus"))
				.withRequestBody(containing("{\"journalstatus\":\"U\",\"fraDato\":\"2019-01-01\",\"journalposttyper\":[\"I\",\"U\",\"N\"],\"foerste\":5,\"etterPeker\":null}")));
		verify(1, postRequestedFor(urlEqualTo("/abac")));
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep5() throws IOException, URISyntaxException {
		abacDenyPep5SkipPep1gPep2Pep2dPep3();

		stubFor(post("/hentjournalsakinfo/finnjournalposterstatus")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposterstatus_single-utgaar.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversiktJournalstatusUtgaar();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(1, dokumentoversikt.getJournalposter().size());
		assertEquals("453466679", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertTrue(dokumentoversikt.getJournalposter().get(0).getDokumenter().isEmpty());
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposterstatus"))
				.withRequestBody(containing("{\"journalstatus\":\"U\",\"fraDato\":\"2019-01-01\",\"journalposttyper\":[\"I\",\"U\",\"N\"],\"foerste\":5,\"etterPeker\":null}")));
		verify(3, postRequestedFor(urlEqualTo("/abac")));
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep6d() throws IOException, URISyntaxException {
		abacDenyPep6d();

		stubFor(post("/hentjournalsakinfo/finnjournalposterstatus")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposterstatus_single-utgaar.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversiktJournalstatusUtgaar();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(1, dokumentoversikt.getJournalposter().size());
		assertEquals("453466679", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertSaksbehandlerHarIkkeTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposterstatus"))
				.withRequestBody(containing("{\"journalstatus\":\"U\",\"fraDato\":\"2019-01-01\",\"journalposttyper\":[\"I\",\"U\",\"N\"],\"foerste\":5,\"etterPeker\":null}")));
		verify(3, postRequestedFor(urlEqualTo("/abac")));
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	}

	private ResponseEntity<LinkedHashMap> callDokumentOversiktJournalstatusUtgaar() throws IOException, URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktJournalstatus/dokumentoversiktjournalstatus_utgaar.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, LinkedHashMap.class);
	}

	private ResponseEntity<LinkedHashMap> callDokumentOversiktJournalstatusUkjentBruker() throws IOException, URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktJournalstatus/dokumentoversiktjournalstatus_ukjent_bruker.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, LinkedHashMap.class);
	}

	private Dokumentoversikt getDokumentoversikt(ResponseEntity<LinkedHashMap> responseEntity) {
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		return objectMapper.convertValue(responseEntityData.get("dokumentoversiktJournalstatus"), Dokumentoversikt.class);
	}
}
