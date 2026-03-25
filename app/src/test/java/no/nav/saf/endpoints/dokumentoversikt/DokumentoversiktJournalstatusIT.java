package no.nav.saf.endpoints.dokumentoversikt;

import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.endpoints.AbstractItest;
import no.nav.saf.endpoints.graphql.GraphQLRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class DokumentoversiktJournalstatusIT extends AbstractItest {

	@BeforeEach
	void setUp() {
		setupHappyPathAzureToken();
	}


	@Test
	void shouldHentDokumentoversiktJournalstatusUkjentBruker() throws URISyntaxException {
		tilgangskontrollPermit();

		stubFor(post("/dokarkiv/finnjournalposterstatus")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/journalpostjournalstatus/journalposter-journalstatus-happy-all.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversiktJournalstatusUkjentBruker();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(4, dokumentoversikt.getJournalposter().size()); // 2 feilregistrert journalpost filterers bort
		assertEquals("639658603", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals(Journalstatus.UKJENT_BRUKER, dokumentoversikt.getJournalposter().get(0).getJournalstatus());
		assertEquals("639658601", dokumentoversikt.getJournalposter().get(1).getJournalpostId());
		assertEquals(Journalstatus.UKJENT_BRUKER, dokumentoversikt.getJournalposter().get(1).getJournalstatus());
		assertEquals("639658521", dokumentoversikt.getJournalposter().get(2).getJournalpostId());
		assertEquals(Journalstatus.UKJENT_BRUKER, dokumentoversikt.getJournalposter().get(2).getJournalstatus());
		assertEquals("639658501", dokumentoversikt.getJournalposter().get(3).getJournalpostId());
		assertEquals(Journalstatus.UKJENT_BRUKER, dokumentoversikt.getJournalposter().get(3).getJournalstatus());

		assertFalse(dokumentoversikt.getSideInfo().isFinnesNesteSide());

		verify(postRequestedFor(urlEqualTo("/dokarkiv/finnjournalposterstatus"))
				.withRequestBody(matchingJsonPath("$.journalstatus", containing("UB"))));
	}

	@Test
	void shouldHentDokumentoversiktJournalstatusUtgaar() throws URISyntaxException {
		tilgangskontrollPermit();

		stubFor(post("/dokarkiv/finnjournalposterstatus")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/journalpostjournalstatus/journalposter-journalstatus-happy-page-1-of-2.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversiktJournalstatusUtgaar();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(4, dokumentoversikt.getJournalposter().size());
		assertEquals("639658603", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals(Journalstatus.UTGAAR, dokumentoversikt.getJournalposter().get(0).getJournalstatus());
		assertEquals("639658601", dokumentoversikt.getJournalposter().get(1).getJournalpostId());
		assertEquals(Journalstatus.UTGAAR, dokumentoversikt.getJournalposter().get(1).getJournalstatus());
		assertEquals("639658521", dokumentoversikt.getJournalposter().get(2).getJournalpostId());
		assertEquals(Journalstatus.UTGAAR, dokumentoversikt.getJournalposter().get(2).getJournalstatus());
		assertEquals("639658501", dokumentoversikt.getJournalposter().get(3).getJournalpostId());
		assertEquals(Journalstatus.UTGAAR, dokumentoversikt.getJournalposter().get(3).getJournalstatus());

		assertNotNull(dokumentoversikt.getSideInfo().getSluttpeker());
		assertTrue(dokumentoversikt.getSideInfo().isFinnesNesteSide());

		verify(postRequestedFor(urlEqualTo("/dokarkiv/finnjournalposterstatus"))
				.withRequestBody(matchingJsonPath("$.journalstatus", containing("U"))));
	}

	@Test
	void shouldFailIfUnsupportedFieldsInQuery() throws URISyntaxException {
		tilgangskontrollPermit();

		stubFor(post("/dokarkiv/finnjournalposterstatus")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/journalpostjournalstatus/journalposter-journalstatus-happy-page-1-of-2.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversiktWithQuery("dokumentoversiktJournalstatus/dokumentoversiktjournalstatus_utgaar_brukerHarTilgang.query");
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(0, dokumentoversikt.getJournalposter().size());
		Map<String,String> errors = ((List<Map>)responseEntity.getBody().get("errors")).getFirst();
		assertThat(errors).isNotNull();
		assertThat(errors.get("message")).contains("Feltene journalposter/dokumenter/dokumentvarianter/brukerHarTilgang, journalposter/dokumenter/dokumentvarianter/brukerTilgangAvvistBegrunnelser og journalposter/brukerTilgangAvvistBegrunnelser er ikke støttet i DokumentoversiktJournalstatus-queriet");
	}

	@Test
	void finnJournalposterStatusTechnicalException() throws URISyntaxException {
		tilgangskontrollPermit();

		stubFor(post("/dokarkiv/finnjournalposterstatus")
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/journalposterjournalstatus/journalposter-journalstatus-exception-technical.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversiktJournalstatusUtgaar();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		verifyEmptyJournalpostListeAndEmptySideInfo(dokumentoversikt);
	}

	@Test
	void finnJournalposterStatusFunctionalException() throws URISyntaxException {
		tilgangskontrollPermit();

		stubFor(post("/dokarkiv/finnjournalposterstatus")
				.willReturn(aResponse().withStatus(HttpStatus.BAD_REQUEST.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/journalposterjournalstatus/journalposter-journalstatus-exception-functional.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversiktJournalstatusUtgaar();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		verifyEmptyJournalpostListeAndEmptySideInfo(dokumentoversikt);
	}

	@Test
	void finnJournalposterStatusEmptyResponse() throws URISyntaxException {
		tilgangskontrollPermit();

		stubFor(post("/dokarkiv/finnjournalposterstatus")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/journalpostjournalstatus/journalposter-journalstatus-empty.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversiktJournalstatusUtgaar();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		verifyEmptyJournalpostListeAndEmptySideInfo(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/dokarkiv/finnjournalposterstatus"))
				.withRequestBody(containing("{\"journalstatus\":\"U\",\"fraDato\":\"2019-01-01\",\"journalposttyper\":[\"I\",\"U\",\"N\"],\"antallRader\":5,\"etterPeker\":null}")));
	}

	@Test
	void shouldGetAuthorized() throws URISyntaxException {
		tilgangskontrollPermit();

		stubFor(post("/dokarkiv/finnjournalposterstatus")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/journalpostjournalstatus/journalposter-journalstatus-single-utgaar.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversiktJournalstatusUtgaar();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(1, dokumentoversikt.getJournalposter().size());
		assertEquals("453466679", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals(1, dokumentoversikt.getJournalposter().get(0).getDokumenter().size());

		assertFalse(dokumentoversikt.getSideInfo().isFinnesNesteSide());

		verify(postRequestedFor(urlEqualTo("/dokarkiv/finnjournalposterstatus"))
				.withRequestBody(containing("{\"journalstatus\":\"U\",\"fraDato\":\"2019-01-01\",\"journalposttyper\":[\"I\",\"U\",\"N\"],\"antallRader\":5,\"etterPeker\":null}")));
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep4() throws URISyntaxException {
		tilgangskontrollDenyPep4();

		stubFor(post("/dokarkiv/finnjournalposterstatus")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/journalpostjournalstatus/journalposter-journalstatus-single-utgaar.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversiktJournalstatusUtgaar();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(0, dokumentoversikt.getJournalposter().size());
		assertFalse(dokumentoversikt.getSideInfo().isFinnesNesteSide());
		verify(postRequestedFor(urlEqualTo("/dokarkiv/finnjournalposterstatus"))
				.withRequestBody(containing("{\"journalstatus\":\"U\",\"fraDato\":\"2019-01-01\",\"journalposttyper\":[\"I\",\"U\",\"N\"],\"antallRader\":5,\"etterPeker\":null}")));
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep5() throws URISyntaxException {
		tilgangskontrollDenyPep5();

		stubFor(post("/dokarkiv/finnjournalposterstatus")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/journalpostjournalstatus/journalposter-journalstatus-single-utgaar.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversiktJournalstatusUtgaar();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertThat(dokumentoversikt.getJournalposter()).hasSize(1);
		assertEquals("453466679", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertThat(dokumentoversikt.getJournalposter().get(0).getDokumenter()).isEmpty();
		verify(postRequestedFor(urlEqualTo("/dokarkiv/finnjournalposterstatus"))
				.withRequestBody(containing("{\"journalstatus\":\"U\",\"fraDato\":\"2019-01-01\",\"journalposttyper\":[\"I\",\"U\",\"N\"],\"antallRader\":5,\"etterPeker\":null}")));
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldGetUnauthorizedFromPep6d() throws URISyntaxException {
		tilgangskontrollDenyPep6d();

		stubFor(post("/dokarkiv/finnjournalposterstatus")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/journalpostjournalstatus/journalposter-journalstatus-single-utgaar-skjermet-dokumentvariant.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversiktJournalstatusUtgaar();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertThat(dokumentoversikt.getJournalposter()).hasSize(1);
		assertEquals("453466679", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertSaksbehandlerHarIkkeTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/dokarkiv/finnjournalposterstatus"))
				.withRequestBody(containing("{\"journalstatus\":\"U\",\"fraDato\":\"2019-01-01\",\"journalposttyper\":[\"I\",\"U\",\"N\"],\"antallRader\":5,\"etterPeker\":null}")));
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
	}

	private ResponseEntity<LinkedHashMap> callDokumentOversiktJournalstatusUtgaar() throws URISyntaxException {
		return callDokumentOversiktWithQuery("dokumentoversiktJournalstatus/dokumentoversiktjournalstatus_utgaar.query");
	}

	private ResponseEntity<LinkedHashMap> callDokumentOversiktJournalstatusUkjentBruker() throws URISyntaxException {
		return callDokumentOversiktWithQuery("dokumentoversiktJournalstatus/dokumentoversiktjournalstatus_ukjent_bruker.query");
	}

	private ResponseEntity<LinkedHashMap> callDokumentOversiktWithQuery(String resourcename) throws URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath(resourcename), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, LinkedHashMap.class);
	}

	private Dokumentoversikt getDokumentoversikt(ResponseEntity<LinkedHashMap> responseEntity) {
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		return jsonMapper.convertValue(responseEntityData.get("dokumentoversiktJournalstatus"), Dokumentoversikt.class);
	}
}
