package no.nav.saf.endpoints.dokumentoversikt;

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
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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
						.withBodyFile("gsak/gsak-sakerBySaksId_not_bid-happy.json")));
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
		assertSaksbehandlerHarTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/aktoerv2")).withRequestBody(matchingXPath("//aktoerId/text()", equalTo(AKTOER_ID))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[\"21998969\"],\"fraDato\":\"0001-01-01\",\"inkluderTema\":[\"AAP\",\"AAR\",\"AGR\",\"BAR\",\"BID\",\"BIL\",\"DAG\",\"ENF\",\"ERS\",\"FAR\",\"FEI\",\"FOR\",\"FOS\",\"FUL\",\"GEN\",\"GRA\",\"GRU\",\"HEL\",\"HJE\",\"IAR\",\"IND\",\"KON\",\"KTR\",\"MED\",\"MOB\",\"OKO\",\"OMS\",\"OPA\",\"OPP\",\"PEN\",\"PER\",\"REH\",\"REK\",\"RPO\",\"RVE\",\"SAA\",\"SAK\",\"SAP\",\"SER\",\"SIK\",\"STO\",\"SUP\",\"SYK\",\"SYM\",\"TIL\",\"TRK\",\"TRY\",\"TSO\",\"TSR\",\"UFM\",\"UFO\",\"UKJ\",\"VEN\",\"YRA\",\"YRK\"],\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"***gammelt_fnr***\"],\"foerste\":3,\"etterPeker\":null,\"siste\":null,\"foerPeker\":null}")));
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
						.withBodyFile("gsak/gsak-sakerBySaksId_not_bid-happy.json")));
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
		assertSaksbehandlerHarTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/aktoerv2")).withRequestBody(matchingXPath("//ident/text()", equalTo(FNR))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[\"21998969\"],\"fraDato\":\"0001-01-01\",\"inkluderTema\":[\"AAP\",\"AAR\",\"AGR\",\"BAR\",\"BID\",\"BIL\",\"DAG\",\"ENF\",\"ERS\",\"FAR\",\"FEI\",\"FOR\",\"FOS\",\"FUL\",\"GEN\",\"GRA\",\"GRU\",\"HEL\",\"HJE\",\"IAR\",\"IND\",\"KON\",\"KTR\",\"MED\",\"MOB\",\"OKO\",\"OMS\",\"OPA\",\"OPP\",\"PEN\",\"PER\",\"REH\",\"REK\",\"RPO\",\"RVE\",\"SAA\",\"SAK\",\"SAP\",\"SER\",\"SIK\",\"STO\",\"SUP\",\"SYK\",\"SYM\",\"TIL\",\"TRK\",\"TRY\",\"TSO\",\"TSR\",\"UFM\",\"UFO\",\"UKJ\",\"VEN\",\"YRA\",\"YRK\"],\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"***gammelt_fnr***\",\"***gammelt_fnr***\"],\"foerste\":3,\"etterPeker\":null,\"siste\":null,\"foerPeker\":null}")));
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
						.withBodyFile("gsak/gsak-sakerBySaksId_not_bid-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter_single_temaForNullskjerming-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithOrgnr();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertSaksbehandlerHarTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.gsakSakIds", containing("135695442"))));
		verify(0, postRequestedFor(urlEqualTo("/aktoerv2")));
		verify(0, postRequestedFor(urlEqualTo("/servicegw")));
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
		stubFor(post("/servicegw")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-empty.xml")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals("429812815", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertSaksbehandlerHarTilgang(dokumentoversikt);
		verify(3, postRequestedFor(urlEqualTo("/abac")));
		verify(postRequestedFor(urlEqualTo("/aktoerv2")).withRequestBody(matchingXPath("//aktoerId/text()", equalTo(AKTOER_ID))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"inkluderTema\":[\"AAP\",\"AAR\",\"AGR\",\"BAR\",\"BID\",\"BIL\",\"DAG\",\"ENF\",\"ERS\",\"FAR\",\"FEI\",\"FOR\",\"FOS\",\"FUL\",\"GEN\",\"GRA\",\"GRU\",\"HEL\",\"HJE\",\"IAR\",\"IND\",\"KON\",\"KTR\",\"MED\",\"MOB\",\"OKO\",\"OMS\",\"OPA\",\"OPP\",\"PEN\",\"PER\",\"REH\",\"REK\",\"RPO\",\"RVE\",\"SAA\",\"SAK\",\"SAP\",\"SER\",\"SIK\",\"STO\",\"SUP\",\"SYK\",\"SYM\",\"TIL\",\"TRK\",\"TRY\",\"TSO\",\"TSR\",\"UFM\",\"UFO\",\"UKJ\",\"VEN\",\"YRA\",\"YRK\"],\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"***gammelt_fnr***\"],\"foerste\":3,\"etterPeker\":null,\"siste\":null,\"foerPeker\":null}")));
		verify(postRequestedFor(urlEqualTo("/servicegw")).withRequestBody(matchingXPath("//personident/text()", equalTo("***gammelt_fnr***"))));
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
		stubFor(post("/servicegw")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy.xml")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertSaksbehandlerHarTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/aktoerv2")).withRequestBody(matchingXPath("//aktoerId/text()", equalTo(AKTOER_ID))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[\"21998969\"],\"fraDato\":\"0001-01-01\",\"inkluderTema\":[\"AAP\",\"AAR\",\"AGR\",\"BAR\",\"BID\",\"BIL\",\"DAG\",\"ENF\",\"ERS\",\"FAR\",\"FEI\",\"FOR\",\"FOS\",\"FUL\",\"GEN\",\"GRA\",\"GRU\",\"HEL\",\"HJE\",\"IAR\",\"IND\",\"KON\",\"KTR\",\"MED\",\"MOB\",\"OKO\",\"OMS\",\"OPA\",\"OPP\",\"PEN\",\"PER\",\"REH\",\"REK\",\"RPO\",\"RVE\",\"SAA\",\"SAK\",\"SAP\",\"SER\",\"SIK\",\"STO\",\"SUP\",\"SYK\",\"SYM\",\"TIL\",\"TRK\",\"TRY\",\"TSO\",\"TSR\",\"UFM\",\"UFO\",\"UKJ\",\"VEN\",\"YRA\",\"YRK\"],\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"***gammelt_fnr***\"],\"foerste\":3,\"etterPeker\":null,\"siste\":null,\"foerPeker\":null}")));
		verify(postRequestedFor(urlEqualTo("/servicegw")).withRequestBody(matchingXPath("//personident/text()", equalTo("***gammelt_fnr***"))));
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
		stubFor(get("/bidrag/*").willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		verify(postRequestedFor(urlEqualTo("/aktoerv2")).withRequestBody(matchingXPath("//aktoerId/text()", equalTo(AKTOER_ID))));
		verify(1, postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")));
		verify(postRequestedFor(urlEqualTo("/servicegw")).withRequestBody(matchingXPath("//personident/text()", equalTo("***gammelt_fnr***"))));
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
		stubFor(post("/servicegw")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-empty.xml")));
		stubFor(get("/bidrag/135695442").willReturn(aResponse()
				.withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/aktoerv2")).withRequestBody(matchingXPath("//aktoerId/text()", equalTo(AKTOER_ID))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.gsakSakIds", containing(""))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.psakSakIds", containing(""))));
		verify(postRequestedFor(urlEqualTo("/servicegw")).withRequestBody(matchingXPath("//personident/text()", equalTo("***gammelt_fnr***"))));
		verify(getRequestedFor(urlEqualTo("/bidrag/135695442")));
	}

	@Test
	public void FinnJournalposterEmptyResponse() throws IOException, URISyntaxException {
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
		stubFor(post("/servicegw")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy.xml")));
		stubFor(get("/bidrag/135695442").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/aktoerv2")).withRequestBody(matchingXPath("//aktoerId/text()", equalTo(AKTOER_ID))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[\"21998969\"],\"fraDato\":\"0001-01-01\",\"inkluderTema\":[\"AAP\",\"AAR\",\"AGR\",\"BAR\",\"BID\",\"BIL\",\"DAG\",\"ENF\",\"ERS\",\"FAR\",\"FEI\",\"FOR\",\"FOS\",\"FUL\",\"GEN\",\"GRA\",\"GRU\",\"HEL\",\"HJE\",\"IAR\",\"IND\",\"KON\",\"KTR\",\"MED\",\"MOB\",\"OKO\",\"OMS\",\"OPA\",\"OPP\",\"PEN\",\"PER\",\"REH\",\"REK\",\"RPO\",\"RVE\",\"SAA\",\"SAK\",\"SAP\",\"SER\",\"SIK\",\"STO\",\"SUP\",\"SYK\",\"SYM\",\"TIL\",\"TRK\",\"TRY\",\"TSO\",\"TSR\",\"UFM\",\"UFO\",\"UKJ\",\"VEN\",\"YRA\",\"YRK\"],\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"***gammelt_fnr***\"],\"foerste\":3,\"etterPeker\":null,\"siste\":null,\"foerPeker\":null}")));
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
		stubFor(get("/bidrag/135695442").willReturn(aResponse().withStatus(HttpStatus.OK.value())
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
		stubFor(post("/servicegw")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-empty.xml")));
		stubFor(get("/bidrag/135695442").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"inkluderTema\":[\"AAP\",\"AAR\",\"AGR\",\"BAR\",\"BID\",\"BIL\",\"DAG\",\"ENF\",\"ERS\",\"FAR\",\"FEI\",\"FOR\",\"FOS\",\"FUL\",\"GEN\",\"GRA\",\"GRU\",\"HEL\",\"HJE\",\"IAR\",\"IND\",\"KON\",\"KTR\",\"MED\",\"MOB\",\"OKO\",\"OMS\",\"OPA\",\"OPP\",\"PEN\",\"PER\",\"REH\",\"REK\",\"RPO\",\"RVE\",\"SAA\",\"SAK\",\"SAP\",\"SER\",\"SIK\",\"STO\",\"SUP\",\"SYK\",\"SYM\",\"TIL\",\"TRK\",\"TRY\",\"TSO\",\"TSR\",\"UFM\",\"UFO\",\"UKJ\",\"VEN\",\"YRA\",\"YRK\"],\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"***gammelt_fnr***\"],\"foerste\":3,\"etterPeker\":null,\"siste\":null,\"foerPeker\":null}")));
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
		stubFor(post("/servicegw")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-empty.xml")));
		stubFor(get("/bidrag/135695442").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));


		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertSaksbehandlerHarIkkeTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"inkluderTema\":[\"AAP\",\"AAR\",\"AGR\",\"BAR\",\"BID\",\"BIL\",\"DAG\",\"ENF\",\"ERS\",\"FAR\",\"FEI\",\"FOR\",\"FOS\",\"FUL\",\"GEN\",\"GRA\",\"GRU\",\"HEL\",\"HJE\",\"IAR\",\"IND\",\"KON\",\"KTR\",\"MED\",\"MOB\",\"OKO\",\"OMS\",\"OPA\",\"OPP\",\"PEN\",\"PER\",\"REH\",\"REK\",\"RPO\",\"RVE\",\"SAA\",\"SAK\",\"SAP\",\"SER\",\"SIK\",\"STO\",\"SUP\",\"SYK\",\"SYM\",\"TIL\",\"TRK\",\"TRY\",\"TSO\",\"TSR\",\"UFM\",\"UFO\",\"UKJ\",\"VEN\",\"YRA\",\"YRK\"],\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"***gammelt_fnr***\"],\"foerste\":3,\"etterPeker\":null,\"siste\":null,\"foerPeker\":null}")));
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
		stubFor(post("/servicegw")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-empty.xml")));
		stubFor(get("/bidrag/135695442").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"inkluderTema\":[\"AAP\",\"AAR\",\"AGR\",\"BAR\",\"BID\",\"BIL\",\"DAG\",\"ENF\",\"ERS\",\"FAR\",\"FEI\",\"FOR\",\"FOS\",\"FUL\",\"GEN\",\"GRA\",\"GRU\",\"HEL\",\"HJE\",\"IAR\",\"IND\",\"KON\",\"KTR\",\"MED\",\"MOB\",\"OKO\",\"OMS\",\"OPA\",\"OPP\",\"PEN\",\"PER\",\"REH\",\"REK\",\"RPO\",\"RVE\",\"SAA\",\"SAK\",\"SAP\",\"SER\",\"SIK\",\"STO\",\"SUP\",\"SYK\",\"SYM\",\"TIL\",\"TRK\",\"TRY\",\"TSO\",\"TSR\",\"UFM\",\"UFO\",\"UKJ\",\"VEN\",\"YRA\",\"YRK\"],\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"***gammelt_fnr***\"],\"foerste\":3,\"etterPeker\":null,\"siste\":null,\"foerPeker\":null}")));
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
		stubFor(post("/servicegw")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-empty.xml")));
		stubFor(get("/bidrag/135695442").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));


		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"inkluderTema\":[\"AAP\",\"AAR\",\"AGR\",\"BAR\",\"BID\",\"BIL\",\"DAG\",\"ENF\",\"ERS\",\"FAR\",\"FEI\",\"FOR\",\"FOS\",\"FUL\",\"GEN\",\"GRA\",\"GRU\",\"HEL\",\"HJE\",\"IAR\",\"IND\",\"KON\",\"KTR\",\"MED\",\"MOB\",\"OKO\",\"OMS\",\"OPA\",\"OPP\",\"PEN\",\"PER\",\"REH\",\"REK\",\"RPO\",\"RVE\",\"SAA\",\"SAK\",\"SAP\",\"SER\",\"SIK\",\"STO\",\"SUP\",\"SYK\",\"SYM\",\"TIL\",\"TRK\",\"TRY\",\"TSO\",\"TSR\",\"UFM\",\"UFO\",\"UKJ\",\"VEN\",\"YRA\",\"YRK\"],\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"***gammelt_fnr***\"],\"foerste\":3,\"etterPeker\":null,\"siste\":null,\"foerPeker\":null}")));
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
		stubFor(post("/servicegw")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-empty.xml")));
		stubFor(get("/bidrag/135695442").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertFalse(dokumentoversikt.getJournalposter().isEmpty());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertTrue(dokumentoversikt.getJournalposter().get(0).getDokumenter().isEmpty());
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"inkluderTema\":[\"AAP\",\"AAR\",\"AGR\",\"BAR\",\"BID\",\"BIL\",\"DAG\",\"ENF\",\"ERS\",\"FAR\",\"FEI\",\"FOR\",\"FOS\",\"FUL\",\"GEN\",\"GRA\",\"GRU\",\"HEL\",\"HJE\",\"IAR\",\"IND\",\"KON\",\"KTR\",\"MED\",\"MOB\",\"OKO\",\"OMS\",\"OPA\",\"OPP\",\"PEN\",\"PER\",\"REH\",\"REK\",\"RPO\",\"RVE\",\"SAA\",\"SAK\",\"SAP\",\"SER\",\"SIK\",\"STO\",\"SUP\",\"SYK\",\"SYM\",\"TIL\",\"TRK\",\"TRY\",\"TSO\",\"TSR\",\"UFM\",\"UFO\",\"UKJ\",\"VEN\",\"YRA\",\"YRK\"],\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"***gammelt_fnr***\"],\"foerste\":3,\"etterPeker\":null,\"siste\":null,\"foerPeker\":null}")));
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
		stubFor(post("/servicegw")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-empty.xml")));
		stubFor(get("/bidrag/135695442").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikBrukerWithAktoerId();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertSaksbehandlerHarIkkeTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"135695442\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"inkluderTema\":[\"AAP\",\"AAR\",\"AGR\",\"BAR\",\"BID\",\"BIL\",\"DAG\",\"ENF\",\"ERS\",\"FAR\",\"FEI\",\"FOR\",\"FOS\",\"FUL\",\"GEN\",\"GRA\",\"GRU\",\"HEL\",\"HJE\",\"IAR\",\"IND\",\"KON\",\"KTR\",\"MED\",\"MOB\",\"OKO\",\"OMS\",\"OPA\",\"OPP\",\"PEN\",\"PER\",\"REH\",\"REK\",\"RPO\",\"RVE\",\"SAA\",\"SAK\",\"SAP\",\"SER\",\"SIK\",\"STO\",\"SUP\",\"SYK\",\"SYM\",\"TIL\",\"TRK\",\"TRY\",\"TSO\",\"TSR\",\"UFM\",\"UFO\",\"UKJ\",\"VEN\",\"YRA\",\"YRK\"],\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[\"***gammelt_fnr***\"],\"foerste\":3,\"etterPeker\":null,\"siste\":null,\"foerPeker\":null}")));
		verifyabacDenyPep6dAndHttpStatusCode(HttpStatus.OK, responseEntity.getStatusCode());
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