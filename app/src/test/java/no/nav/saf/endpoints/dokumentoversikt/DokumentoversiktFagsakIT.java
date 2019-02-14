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
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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
public class DokumentoversiktFagsakIT extends AbstractItest {

	private static final String FAGSAK_ID = "***gammelt_fnr***59";
	private static final String FAGSAK_SYSTEM = "AO01";

	private ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void shouldHentDokumentoversiktFagsakWithFagsakIdGSAK() throws IOException, URISyntaxException {
		abacPermit();

		stubFor(get("/gsak?fagsakNr=" + FAGSAK_ID + "&applikasjon=" + FAGSAK_SYSTEM)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakerByFagsakIdAndFagsaksystem-happy.json")));

		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter-happy.json")));

		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerIdListe-happy.xml")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

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
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("psak/psak-hentBrukerForSak-happy.json")));

		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter-happy.json")));

		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentAktoerIdForIdent-happy.xml")));

		stubFor(post("/servicegw")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy.xml")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakPsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertEquals("429837329", dokumentoversikt.getJournalposter().get(1).getJournalpostId());
		assertEquals("429812815", dokumentoversikt.getJournalposter().get(2).getJournalpostId());

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter")).withRequestBody(matchingJsonPath("$.psakSakIds", containing("21998969"))));
	}

	@Test
	public void shouldHentDokumentoversiktFagsakWithFagsakIDSladdet() throws IOException, URISyntaxException {
		abacPermit();

		stubFor(get("/gsak?fagsakNr=" + FAGSAK_ID + "&applikasjon=" + FAGSAK_SYSTEM)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakerByFagsakIdAndFagsaksystem-happy.json")));

		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/finnjournalposter_single_sladdet-happy.json")));

		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerIdListe-happy.xml")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
	}

	@Test
	public void hentSakerTechnicalFail() throws IOException, URISyntaxException {
		abacDenyPep1g();
		stubFor(get("/gsak?fagsakNr=***gammelt_fnr***59&applikasjon=AO01")
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(0, dokumentoversikt.getJournalposter().size());
	}

	@Test
	public void hentSakerFunctionalFail() throws IOException, URISyntaxException {
		abacDenyPep1g();
		stubFor(get("/gsak?fagsakNr=***gammelt_fnr***59&applikasjon=AO01")
				.willReturn(aResponse().withStatus(HttpStatus.BAD_REQUEST.value())));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(0, dokumentoversikt.getJournalposter().size());
	}


	@Test
	public void shouldHentDokumentoversiktFagsakAktoerTechnicalFail() throws IOException, URISyntaxException {
		abacPermit();

		stubFor(get("/pensjonsakrs")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("psak/psak-hentBrukerForSak-happy.json")));

		stubFor(get("/gsak?fagsakNr=" + FAGSAK_ID + "&applikasjon=" + FAGSAK_SYSTEM)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakerByFagsakIdAndFagsaksystem-happy.json")));

		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter-empty.json")));

		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerIdListe-technical.xml")));


		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(0, dokumentoversikt.getJournalposter().size());
	}

	@Test
	public void shouldHentDokumentoversikHentSakSammendragListeTechnicalFail() throws IOException, URISyntaxException {
		abacPermit();

		stubFor(get("/pensjonsakrs")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("psak/psak-hentBrukerForSak-happy.json")));

		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter-empty.json")));

		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentAktoerIdForIdent-happy.xml")));

		stubFor(post("/servicegw")
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())
						.withBodyFile("psak-hentSakSammendragListe-technical.xml")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakPsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(0, dokumentoversikt.getJournalposter().size());
	}

	@Test
	public void shouldHentDokumentoversikHentSakSammendragListeFunctionalFail() throws IOException, URISyntaxException {
		abacPermit();

		stubFor(get("/pensjonsakrs")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("psak/psak-hentBrukerForSak-happy.json")));

		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter-empty.json")));

		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentAktoerIdForIdent-happy.xml")));

		stubFor(post("/servicegw")
				.willReturn(aResponse().withStatus(HttpStatus.BAD_REQUEST.value())
						.withBodyFile("psak-hentSakSammendragListe-technical.xml")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakPsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		assertEquals(0, dokumentoversikt.getJournalposter().size());
	}

	@Test
	public void FinnJournalposterEmptyResponse() throws IOException, URISyntaxException {
		abacPermit();

		stubFor(get("/gsak?fagsakNr=" + FAGSAK_ID + "&applikasjon=" + FAGSAK_SYSTEM)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakerByFagsakIdAndFagsaksystem-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter-empty.json")));
		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerIdListe-happy.xml")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/aktoerv2")).withRequestBody(matchingXPath("//aktoerIdListe/text()", equalTo(FAGSAK_ID))));
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"119185782\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"inkluderTema\":[\"AAP\",\"AAR\",\"AGR\",\"BAR\",\"BID\",\"BIL\",\"DAG\",\"ENF\",\"ERS\",\"FAR\",\"FEI\",\"FOR\",\"FOS\",\"FUL\",\"GEN\",\"GRA\",\"GRU\",\"HEL\",\"HJE\",\"IAR\",\"IND\",\"KON\",\"KTR\",\"MED\",\"MOB\",\"OKO\",\"OMS\",\"OPA\",\"OPP\",\"PEN\",\"PER\",\"REH\",\"REK\",\"RPO\",\"RVE\",\"SAA\",\"SAK\",\"SAP\",\"SER\",\"SIK\",\"STO\",\"SUP\",\"SYK\",\"SYM\",\"TIL\",\"TRK\",\"TRY\",\"TSO\",\"TSR\",\"UFM\",\"UFO\",\"UKJ\",\"VEN\",\"YRA\",\"YRK\"],\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[],\"foerste\":5,\"etterPeker\":null,\"siste\":null,\"foerPeker\":null}")));
	}


	@Test
	public void shouldGetUnauthorizedFromPep1g() throws IOException, URISyntaxException {
		abacDenyPep1g();

		stubFor(get("/gsak?fagsakNr=" + FAGSAK_ID + "&applikasjon=" + FAGSAK_SYSTEM)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakerByFagsakIdAndFagsaksystem-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter-happy.json")));
		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerIdListe-happy.xml")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals(3, dokumentoversikt.getJournalposter().size());
		assertNull(dokumentoversikt.getSideInfo());
		verifyabacDenyPep2AndHttpStatusCode(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	public void shouldGetUnauthorizedFromPep2() throws IOException, URISyntaxException {
		abacDenyPep2();
		stubFor(get("/gsak?fagsakNr=" + FAGSAK_ID + "&applikasjon=" + FAGSAK_SYSTEM)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakerByFagsakIdAndFagsaksystem-FAR-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter-empty.json")));
		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerIdListe-happy.xml")));
		stubFor(get("/bidrag/112233445").willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"inkluderTema\":[\"AAP\",\"AAR\",\"AGR\",\"BAR\",\"BID\",\"BIL\",\"DAG\",\"ENF\",\"ERS\",\"FAR\",\"FEI\",\"FOR\",\"FOS\",\"FUL\",\"GEN\",\"GRA\",\"GRU\",\"HEL\",\"HJE\",\"IAR\",\"IND\",\"KON\",\"KTR\",\"MED\",\"MOB\",\"OKO\",\"OMS\",\"OPA\",\"OPP\",\"PEN\",\"PER\",\"REH\",\"REK\",\"RPO\",\"RVE\",\"SAA\",\"SAK\",\"SAP\",\"SER\",\"SIK\",\"STO\",\"SUP\",\"SYK\",\"SYM\",\"TIL\",\"TRK\",\"TRY\",\"TSO\",\"TSR\",\"UFM\",\"UFO\",\"UKJ\",\"VEN\",\"YRA\",\"YRK\"],\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[],\"foerste\":5,\"etterPeker\":null,\"siste\":null,\"foerPeker\":null}")));
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verifyabacDenyPep2AndHttpStatusCode(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	public void shouldGetUnauthorizedFromPep2d() throws IOException, URISyntaxException {
		abacDenyPep2d();
		stubFor(get("/gsak?fagsakNr=" + FAGSAK_ID + "&applikasjon=" + FAGSAK_SYSTEM)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakerByFagsakIdAndFagsaksystem-FAR-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter_single_bidragAndSkjerming-happy.json")));
		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerIdListe-happy.xml")));
		stubFor(get("/bidrag/112233445").willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"112233445\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"inkluderTema\":[\"AAP\",\"AAR\",\"AGR\",\"BAR\",\"BID\",\"BIL\",\"DAG\",\"ENF\",\"ERS\",\"FAR\",\"FEI\",\"FOR\",\"FOS\",\"FUL\",\"GEN\",\"GRA\",\"GRU\",\"HEL\",\"HJE\",\"IAR\",\"IND\",\"KON\",\"KTR\",\"MED\",\"MOB\",\"OKO\",\"OMS\",\"OPA\",\"OPP\",\"PEN\",\"PER\",\"REH\",\"REK\",\"RPO\",\"RVE\",\"SAA\",\"SAK\",\"SAP\",\"SER\",\"SIK\",\"STO\",\"SUP\",\"SYK\",\"SYM\",\"TIL\",\"TRK\",\"TRY\",\"TSO\",\"TSR\",\"UFM\",\"UFO\",\"UKJ\",\"VEN\",\"YRA\",\"YRK\"],\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[],\"foerste\":5,\"etterPeker\":null,\"siste\":null,\"foerPeker\":null}")));
		verifyabacDenyPep2dAndHttpStatusCode(true, HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	public void shouldGetUnauthorizedFromPep3() throws IOException, URISyntaxException {
		abacDenyPep3();
		stubFor(get("/gsak?fagsakNr=" + FAGSAK_ID + "&applikasjon=" + FAGSAK_SYSTEM)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakerByFagsakIdAndFagsaksystem-FAR-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter-empty.json")));
		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerIdListe-happy.xml")));
		stubFor(get("/bidrag/112233445").willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"inkluderTema\":[\"AAP\",\"AAR\",\"AGR\",\"BAR\",\"BID\",\"BIL\",\"DAG\",\"ENF\",\"ERS\",\"FAR\",\"FEI\",\"FOR\",\"FOS\",\"FUL\",\"GEN\",\"GRA\",\"GRU\",\"HEL\",\"HJE\",\"IAR\",\"IND\",\"KON\",\"KTR\",\"MED\",\"MOB\",\"OKO\",\"OMS\",\"OPA\",\"OPP\",\"PEN\",\"PER\",\"REH\",\"REK\",\"RPO\",\"RVE\",\"SAA\",\"SAK\",\"SAP\",\"SER\",\"SIK\",\"STO\",\"SUP\",\"SYK\",\"SYM\",\"TIL\",\"TRK\",\"TRY\",\"TSO\",\"TSR\",\"UFM\",\"UFO\",\"UKJ\",\"VEN\",\"YRA\",\"YRK\"],\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[],\"foerste\":5,\"etterPeker\":null,\"siste\":null,\"foerPeker\":null}")));
		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verifyabacDenyPep3AndHttpStatusCode(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	public void shouldGetUnauthorizedFromPep4() throws IOException, URISyntaxException {
		abacDenyPep4();
		stubFor(get("/gsak?fagsakNr=" + FAGSAK_ID + "&applikasjon=" + FAGSAK_SYSTEM)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakerByFagsakIdAndFagsaksystem-FAR-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter_single_bidragAndSkjerming-happy.json")));
		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerIdListe-happy.xml")));
		stubFor(get("/bidrag/112233445").willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		verifyEmptyJournalpostListeAndNullSideInfo(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"112233445\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"inkluderTema\":[\"AAP\",\"AAR\",\"AGR\",\"BAR\",\"BID\",\"BIL\",\"DAG\",\"ENF\",\"ERS\",\"FAR\",\"FEI\",\"FOR\",\"FOS\",\"FUL\",\"GEN\",\"GRA\",\"GRU\",\"HEL\",\"HJE\",\"IAR\",\"IND\",\"KON\",\"KTR\",\"MED\",\"MOB\",\"OKO\",\"OMS\",\"OPA\",\"OPP\",\"PEN\",\"PER\",\"REH\",\"REK\",\"RPO\",\"RVE\",\"SAA\",\"SAK\",\"SAP\",\"SER\",\"SIK\",\"STO\",\"SUP\",\"SYK\",\"SYM\",\"TIL\",\"TRK\",\"TRY\",\"TSO\",\"TSR\",\"UFM\",\"UFO\",\"UKJ\",\"VEN\",\"YRA\",\"YRK\"],\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[],\"foerste\":5,\"etterPeker\":null,\"siste\":null,\"foerPeker\":null}")));
		verifyabacDenyPep4AndHttpStatusCode(HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	public void shouldGetUnauthorizedFromPep5() throws IOException, URISyntaxException {
		abacDenyPep5();
		stubFor(get("/gsak?fagsakNr=" + FAGSAK_ID + "&applikasjon=" + FAGSAK_SYSTEM)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakerByFagsakIdAndFagsaksystem-FAR-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter_single_bidragAndSkjerming-happy.json")));
		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerIdListe-happy.xml")));
		stubFor(get("/bidrag/112233445").willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertFalse(dokumentoversikt.getJournalposter().isEmpty());
		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertTrue(dokumentoversikt.getJournalposter().get(0).getDokumenter().isEmpty());
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"112233445\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"inkluderTema\":[\"AAP\",\"AAR\",\"AGR\",\"BAR\",\"BID\",\"BIL\",\"DAG\",\"ENF\",\"ERS\",\"FAR\",\"FEI\",\"FOR\",\"FOS\",\"FUL\",\"GEN\",\"GRA\",\"GRU\",\"HEL\",\"HJE\",\"IAR\",\"IND\",\"KON\",\"KTR\",\"MED\",\"MOB\",\"OKO\",\"OMS\",\"OPA\",\"OPP\",\"PEN\",\"PER\",\"REH\",\"REK\",\"RPO\",\"RVE\",\"SAA\",\"SAK\",\"SAP\",\"SER\",\"SIK\",\"STO\",\"SUP\",\"SYK\",\"SYM\",\"TIL\",\"TRK\",\"TRY\",\"TSO\",\"TSR\",\"UFM\",\"UFO\",\"UKJ\",\"VEN\",\"YRA\",\"YRK\"],\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[],\"foerste\":5,\"etterPeker\":null,\"siste\":null,\"foerPeker\":null}")));
		verifyabacDenyPep5AndHttpStatusCode(true, HttpStatus.OK, responseEntity.getStatusCode());
	}

	@Test
	public void shouldGetUnauthorizedFromPep6d() throws IOException, URISyntaxException {
		abacDenyPep6d();

		stubFor(get("/gsak?fagsakNr=" + FAGSAK_ID + "&applikasjon=" + FAGSAK_SYSTEM)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakerByFagsakIdAndFagsaksystem-FAR-happy.json")));
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/finnjournalposter_single_bidragAndSkjerming-happy.json")));
		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerIdListe-happy.xml")));
		stubFor(get("/bidrag/112233445").willReturn(aResponse()
				.withStatus(HttpStatus.OK.value())
				.withHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callDokumentOversikFagsakGsak();
		Dokumentoversikt dokumentoversikt = getDokumentoversikt(responseEntity);

		assertEquals("429837417", dokumentoversikt.getJournalposter().get(0).getJournalpostId());
		assertSaksbehandlerHarIkkeTilgang(dokumentoversikt);
		verify(postRequestedFor(urlEqualTo("/hentjournalsakinfo/finnjournalposter"))
				.withRequestBody(containing("{\"gsakSakIds\":[\"112233445\"],\"psakSakIds\":[],\"fraDato\":\"0001-01-01\",\"inkluderTema\":[\"AAP\",\"AAR\",\"AGR\",\"BAR\",\"BID\",\"BIL\",\"DAG\",\"ENF\",\"ERS\",\"FAR\",\"FEI\",\"FOR\",\"FOS\",\"FUL\",\"GEN\",\"GRA\",\"GRU\",\"HEL\",\"HJE\",\"IAR\",\"IND\",\"KON\",\"KTR\",\"MED\",\"MOB\",\"OKO\",\"OMS\",\"OPA\",\"OPP\",\"PEN\",\"PER\",\"REH\",\"REK\",\"RPO\",\"RVE\",\"SAA\",\"SAK\",\"SAP\",\"SER\",\"SIK\",\"STO\",\"SUP\",\"SYK\",\"SYM\",\"TIL\",\"TRK\",\"TRY\",\"TSO\",\"TSR\",\"UFM\",\"UFO\",\"UKJ\",\"VEN\",\"YRA\",\"YRK\"],\"inkluderJournalStatus\":[\"FL\",\"FS\",\"J\",\"E\"],\"inkluderJournalpostType\":[\"I\",\"U\",\"N\"],\"visFeilregistrerte\":false,\"alleIdenter\":[],\"foerste\":5,\"etterPeker\":null,\"siste\":null,\"foerPeker\":null}")));
		verifyabacDenyPep6dAndHttpStatusCode(HttpStatus.OK, responseEntity.getStatusCode());
	}

	private ResponseEntity<LinkedHashMap> callDokumentOversikFagsakPsak() throws IOException, URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktFagsak/query-fagsakid-psak.json"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, LinkedHashMap.class);
	}

	private ResponseEntity<LinkedHashMap> callDokumentOversikFagsakGsak() throws IOException, URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktFagsak/query-fagsakid-gsak.json"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, LinkedHashMap.class);
	}

	private Dokumentoversikt getDokumentoversikt(ResponseEntity<LinkedHashMap> responseEntity) {
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		return objectMapper.convertValue(responseEntityData.get("dokumentoversiktFagsak"), Dokumentoversikt.class);
	}
}