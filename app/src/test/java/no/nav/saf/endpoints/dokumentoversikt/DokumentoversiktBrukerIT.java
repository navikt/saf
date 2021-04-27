package no.nav.saf.endpoints.dokumentoversikt;

import com.fasterxml.jackson.databind.ObjectMapper;
import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.endpoints.AbstractItest;
import no.nav.saf.endpoints.GraphQLRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@DirtiesContext
public class DokumentoversiktBrukerIT extends AbstractItest {

	private static final String AKTOER_ID = "1912374211459";
	private static final String FNR = "11111111111";
	private static final String ORG_NR = "201545004";
	private static final String KANAL_REFERANSE_ID = "KANAL REFERANSE ID";

	private ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void shouldHentDokumentoversiktBrukerWithAktoerID() throws Exception {
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
						.withBodyFile("gsak/gsak-sakerBySaksId_not_bid-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter-happy.json")));
		stubFor(post("/pensjonsakv1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy.xml")));

		await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
			ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
			Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);
			assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
			assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
			assertEquals("429837329", dokumentoversikt.getJournalposter().get(1).getJournalpostId());
			assertEquals("429812815", dokumentoversikt.getJournalposter().get(2).getJournalpostId());
			assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(0).getEksternReferanseId());
			assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(1).getEksternReferanseId());
			assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(2).getEksternReferanseId());
			assertSaksbehandlerHarTilgang(dokumentoversikt);
			verify(postRequestedFor(urlEqualTo("/aktoerv2")).withRequestBody(matchingXPath("//aktoerId/text()", equalTo(AKTOER_ID))));
			verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
					.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[\"21998969\"],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"20026900817\"],\"foerste\":3,\"etterPeker\":null}")));
			verify(postRequestedFor(urlEqualTo("/pensjonsakv1")).withRequestBody(matchingXPath("//personident/text()", equalTo("20026900817"))));
		});
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
						.withBodyFile("gsak/gsak-sakerBySaksId_not_bid-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter-happy.json")));
		stubFor(post("/pensjonsakv1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy.xml")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithFnr();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
			assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
			assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
			assertEquals("429837329", dokumentoversikt.getJournalposter().get(1).getJournalpostId());
			assertEquals("429812815", dokumentoversikt.getJournalposter().get(2).getJournalpostId());
			assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(0).getEksternReferanseId());
			assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(1).getEksternReferanseId());
			assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(2).getEksternReferanseId());
			assertSaksbehandlerHarTilgang(dokumentoversikt);
			verify(postRequestedFor(urlEqualTo("/aktoerv2")).withRequestBody(matchingXPath("//ident/text()", equalTo(FNR))));
			verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
					.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[\"21998969\"],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"11111111111\"],\"foerste\":3,\"etterPeker\":null}")));
			verify(postRequestedFor(urlEqualTo("/pensjonsakv1")).withRequestBody(matchingXPath("//personident/text()", equalTo("11111111111"))));
		});
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
						.withBodyFile("gsak/gsak-sakerBySaksId_not_bid-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter_single_temaForNullskjerming-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithOrgnr();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals(KANAL_REFERANSE_ID, dokumentoversikt.getJournalposter().get(0).getEksternReferanseId());
		assertSaksbehandlerHarTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.gsakSakIds", containing("135695442"))));
		verify(0, postRequestedFor(urlEqualTo("/aktoerv2")));
		verify(0, postRequestedFor(urlEqualTo("/pensjonsakv1")));
	}

	@Test
	public void shouldHentDokumentoversiktBrukerWithFraDato() throws IOException, URISyntaxException {
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
						.withBodyFile("gsak/gsak-sakerBySaksId_not_bid-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter-happy.json")));


		Map<String, Object> variables = new HashMap<>();
		variables.put("fnr", "11111111111");
		variables.put("fraDato", "2020-06-23");

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithFraDato(variables);
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertFalse(dokumentoversikt.getJournalposter().isEmpty());
	}

	@Test
	public void shouldHentDokumentoversiktBrukerWithAktoerIDMidlertidig() throws IOException, URISyntaxException {
		abacPermit();
		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerId-happy.xml")));
		stubFor(post("/sts")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("sts/sts-happy.xml")));
		stubFor(get("/gsak?aktoerId=" + AKTOER_ID)
				.willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("gsak/gsak-sakerBySaksId-empty.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter-midlertidig-happy.json")));
		stubFor(post("/pensjonsakv1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-empty.xml")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
			assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
			assertEquals("429812815", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
			assertSaksbehandlerHarTilgang(dokumentoversikt);
			verify(2, postRequestedFor(urlEqualTo("/abac")));
			verify(postRequestedFor(urlEqualTo("/aktoerv2")).withRequestBody(matchingXPath("//aktoerId/text()", equalTo(AKTOER_ID))));
			verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
					.withRequestBody(containing("{\"gsakSakIds\":[],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"20026900817\"],\"foerste\":3,\"etterPeker\":null}")));
			verify(postRequestedFor(urlEqualTo("/pensjonsakv1")).withRequestBody(matchingXPath("//personident/text()", equalTo("20026900817"))));
		});
	}

	@Test
	public void shouldHentDokumentoversiktBrukerWithAktoerIDSladdet() throws IOException, URISyntaxException {
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
						.withBodyFile("gsak/gsak-sakerBySaksId_not_bid-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter_single_sladdet-happy.json")));
		stubFor(post("/pensjonsakv1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy.xml")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
			assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
			assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
			assertSaksbehandlerHarTilgang(dokumentoversikt);
			verify(postRequestedFor(urlEqualTo("/aktoerv2")).withRequestBody(matchingXPath("//aktoerId/text()", equalTo(AKTOER_ID))));
			verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
					.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[\"21998969\"],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"20026900817\"],\"foerste\":3,\"etterPeker\":null}")));
			verify(postRequestedFor(urlEqualTo("/pensjonsakv1")).withRequestBody(matchingXPath("//personident/text()", equalTo("20026900817"))));
		});
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
		verify(0, postRequestedFor(urlEqualTo("/pensjonsakv1")));
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
		verify(0, postRequestedFor(urlEqualTo("/pensjonsakv1")));
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
		stubFor(post("/pensjonsakv1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy.xml")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
			verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
			assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
			verify(postRequestedFor(urlEqualTo("/aktoerv2")).withRequestBody(matchingXPath("//aktoerId/text()", equalTo(AKTOER_ID))));
			verify(1, postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")));
			verify(postRequestedFor(urlEqualTo("/pensjonsakv1")).withRequestBody(matchingXPath("//personident/text()", equalTo("20026900817"))));
		});
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
		stubFor(post("/pensjonsakv1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy.xml")));
		stubFor(get("/bidrag/*").willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
			verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
			assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
			verify(postRequestedFor(urlEqualTo("/aktoerv2")).withRequestBody(matchingXPath("//aktoerId/text()", equalTo(AKTOER_ID))));
			verify(1, postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")));
			verify(postRequestedFor(urlEqualTo("/pensjonsakv1")).withRequestBody(matchingXPath("//personident/text()", equalTo("20026900817"))));
		});
	}

	@Test
	public void bidragConsumerTechnicalError() throws IOException, URISyntaxException {
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
		stubFor(post("/pensjonsakv1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-empty.xml")));
		stubFor(get("/bidrag/654321").willReturn(aResponse()
				.withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
			assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
			verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
			verify(postRequestedFor(urlEqualTo("/aktoerv2")).withRequestBody(matchingXPath("//aktoerId/text()", equalTo(AKTOER_ID))));
			verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.gsakSakIds", containing(""))));
			verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.psakSakIds", containing(""))));
			verify(postRequestedFor(urlEqualTo("/pensjonsakv1")).withRequestBody(matchingXPath("//personident/text()", equalTo("20026900817"))));
			verify(getRequestedFor(urlEqualTo("/bidrag/654321")));
		});
	}

	@Test
	public void FinnJournalposterEmptyResponse() {
		abacPermit();
		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerId-happy.xml")));
		stubFor(post("/sts")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("sts/sts-happy.xml")));
		stubFor(get("/gsak?aktoerId=" + AKTOER_ID)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakerBySaksId-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter-empty.json")));
		stubFor(post("/pensjonsakv1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy.xml")));
		stubFor(get("/bidrag/654321").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		//Denne testen er for some reason veldig brittle.
		//At some point funka den uten await, nå trenger den ~31s for å fullføre
		await().atMost(Duration.ofSeconds(60)).untilAsserted(() -> {
			ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
			Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);
			assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
			verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
			verify(postRequestedFor(urlEqualTo("/aktoerv2")).withRequestBody(matchingXPath("//aktoerId/text()", equalTo(AKTOER_ID))));
			verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
					.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[\"21998969\"],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"20026900817\"],\"foerste\":3,\"etterPeker\":null}")));
			verify(postRequestedFor(urlEqualTo("/pensjonsakv1")).withRequestBody(matchingXPath("//personident/text()", equalTo("20026900817"))));
		});
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
		stubFor(post("/pensjonsakv1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy.xml")));
		stubFor(get("/bidrag/654321").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

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
						.withBodyFile("joark/finnjournalposter-empty.json")));
		stubFor(post("/pensjonsakv1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-empty.xml")));
		stubFor(get("/bidrag/654321").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"20026900817\"],\"foerste\":3,\"etterPeker\":null}")));
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verifyabacDenyPep2AndHttpStatusCode(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	public void shouldGetUnauthorizedFromPep2d() throws IOException, URISyntaxException {
		abacDenyPep2d();
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
		stubFor(post("/pensjonsakv1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-empty.xml")));
		stubFor(get("/bidrag/654321").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));


		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertSaksbehandlerHarIkkeTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"20026900817\"],\"foerste\":3,\"etterPeker\":null}")));
		verifyabacDenyPep2dAndHttpStatusCode(true, HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	public void shouldGetUnauthorizedFromPep3() throws IOException, URISyntaxException {
		abacDenyPep3();
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
		stubFor(post("/pensjonsakv1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-empty.xml")));
		stubFor(get("/bidrag/654321").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"20026900817\"],\"foerste\":3,\"etterPeker\":null}")));
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verifyabacDenyPep3AndHttpStatusCode(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	public void shouldGetUnauthorizedFromPep4() throws IOException, URISyntaxException {
		abacDenyPep4();
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
		stubFor(post("/pensjonsakv1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-empty.xml")));
		stubFor(get("/bidrag/654321").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));


		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"20026900817\"],\"foerste\":3,\"etterPeker\":null}")));
		verifyabacDenyPep4AndHttpStatusCode(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	public void shouldGetUnauthorizedFromPep5() throws IOException, URISyntaxException {
		abacDenyPep5();
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
		stubFor(post("/pensjonsakv1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-empty.xml")));
		stubFor(get("/bidrag/654321").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertFalse(dokumentoversikt.getJournalposter().isEmpty());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertTrue(dokumentoversikt.getJournalposter().get(0).getDokumenter().isEmpty());
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"20026900817\"],\"foerste\":3,\"etterPeker\":null}")));
		verifyabacDenyPep5AndHttpStatusCode(true, HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	public void shouldGetUnauthorizedFromPep6d() throws IOException, URISyntaxException {
		abacDenyPep6d();
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
		stubFor(post("/pensjonsakv1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-empty.xml")));
		stubFor(get("/bidrag/654321").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertSaksbehandlerHarIkkeTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"tilDato\":null,\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"20026900817\"],\"foerste\":3,\"etterPeker\":null}")));
		verifyabacDenyPep6dAndHttpStatusCode(HttpStatus.OK, responseEntity.getStatusCode());
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