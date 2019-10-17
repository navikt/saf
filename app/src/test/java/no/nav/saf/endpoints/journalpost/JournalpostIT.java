package no.nav.saf.endpoints.journalpost;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static no.nav.saf.query.journalpost.JournalpostCoordinatorImpl.PEP1G_ERRORMESSAGE;
import static no.nav.saf.query.journalpost.JournalpostCoordinatorImpl.PEP2_ERRORMESSAGE;
import static no.nav.saf.query.journalpost.JournalpostCoordinatorImpl.PEP3_ERRORMESSAGE;
import static no.nav.saf.query.journalpost.JournalpostCoordinatorImpl.PEP4_ERRORMESSAGE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Datotype;
import no.nav.saf.domain.kode.Dokumentstatus;
import no.nav.saf.domain.kode.Journalposttype;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.kode.Kanal;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.kode.Variantformat;
import no.nav.saf.domain.visningsmodell.BrukerIdType;
import no.nav.saf.domain.visningsmodell.DokumentInfo;
import no.nav.saf.domain.visningsmodell.Journalpost;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class JournalpostIT extends AbstractItest {
	private final String JOURNALPOST_ID = "400000000";
	private final String GSAK_ID = "100000000";
	private final String BIDRAG_SAK_ID = "abc123";

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	static {
		OBJECT_MAPPER.registerModule(new JavaTimeModule());
	}

	@Test
	void shouldQueryJournalpostByJournalpostIdWhenAllAccessPermit() throws Exception {
		abacPermit();

		stubFor(get("/hentjournalsakinfo/hentjournalpost/" + JOURNALPOST_ID)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/hentjournalpost_not_bid-happy.json")));

		Journalpost journalpost = parseJournalpost(journalpostQuery());

		assertThat(journalpost.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(journalpost.getTittel(), is("En søknad om noe"));
		assertThat(journalpost.getJournalposttype(), is(Journalposttype.U));
		assertThat(journalpost.getJournalstatus(), is(Journalstatus.FERDIGSTILT));
		assertThat(journalpost.getTema(), is(Tema.FOR));
		assertThat(journalpost.getTemanavn(), is(Tema.FOR.getTemanavn()));
		assertThat(journalpost.getBehandlingstema(), is("sok1"));
		assertThat(journalpost.getBehandlingstemanavn(), is("En viktig søknad"));
		assertThat(journalpost.getSak().getArkivsaksnummer(), is("100000000"));
		assertThat(journalpost.getSak().getArkivsaksystem(), is(Arkivsakssystem.GSAK));
		assertThat(journalpost.getSak().getFagsakId(), is("abc123"));
		assertThat(journalpost.getSak().getFagsaksystem(), is("FS22"));
		assertThat(journalpost.getSak().getDatoOpprettet(), notNullValue());
		assertThat(journalpost.getSak().getArkivsaksnummer(), is("100000000"));
		assertThat(journalpost.getBruker().getId(), is("***gammelt_fnr***00"));
		assertThat(journalpost.getBruker().getType(), is(BrukerIdType.AKTOERID));
		assertThat(journalpost.getAvsenderMottaker().getId(), is("***gammelt_fnr***"));
		assertThat(journalpost.getAvsenderMottaker().getNavn(), is("Bjarne Betjent"));
		assertThat(journalpost.getAvsenderMottaker().getLand(), is("NO"));
		assertTrue(journalpost.getAvsenderMottaker().isErLikBruker());
		assertThat(journalpost.getAvsenderMottakerId(), is("***gammelt_fnr***"));
		assertThat(journalpost.getAvsenderMottakerNavn(), is("Bjarne Betjent"));
		assertThat(journalpost.getAvsenderMottakerLand(), is("NO"));
		assertThat(journalpost.getJournalforendeEnhet(), is("2990"));
		assertThat(journalpost.getJournalfoerendeEnhet(), is("2990"));
		assertThat(journalpost.getJournalfortAvNavn(), is("Max Mekker"));
		assertThat(journalpost.getOpprettetAvNavn(), is("Max Mekker"));
		assertThat(journalpost.getKanal(), is(Kanal.SDP));
		assertThat(journalpost.getKanalnavn(), is(Kanal.SDP.getKanalnavn()));
		assertThat(journalpost.getDatoOpprettet(), notNullValue());
		assertThat(journalpost.getRelevanteDatoer().get(0).getDatotype(), is(Datotype.DATO_EKSPEDERT));
		assertThat(journalpost.getTilleggsopplysninger().get(0).getNokkel(), is("min_nokkel"));
		assertThat(journalpost.getTilleggsopplysninger().get(0).getVerdi(), is("min_verdi"));
		DokumentInfo dokumentInfo1 = journalpost.getDokumenter().get(0);
		assertThat(dokumentInfo1.getDokumentInfoId(), is("500000000"));
		assertThat(dokumentInfo1.getTittel(), is("Dokument1"));
		assertThat(dokumentInfo1.getBrevkode(), is("for123"));
		assertThat(dokumentInfo1.getDokumentstatus(), is(Dokumentstatus.FERDIGSTILT));
		assertThat(dokumentInfo1.getOriginalJournalpostId(), is(JOURNALPOST_ID));
		assertThat(dokumentInfo1.getLogiskeVedlegg().get(0).getTittel(), is("Hei"));
		assertThat(dokumentInfo1.getDokumentvarianter().get(0).getVariantformat(), is(Variantformat.ARKIV));
		assertTrue(dokumentInfo1.getDokumentvarianter().get(0).isSaksbehandlerHarTilgang());
		assertThat(dokumentInfo1.getDokumentvarianter().get(0).getFiltype(), is("PDF"));
		assertThat(dokumentInfo1.getDokumentvarianter().get(0).getFilnavn(), is("filNavn"));
		assertThat(dokumentInfo1.getDokumentvarianter().get(0).getFiluuid(), is("0c0bacf-c233-4a54-96fc-e205b79862d9"));
	}

	@Test
	void shouldQueryJournalpostWhenSakNotFound() throws Exception {
		abacPermit();

		stubFor(get("/hentjournalsakinfo/hentjournalpost/" + JOURNALPOST_ID)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/hentjournalpost_not_bid-null-user-and-sak-happy.json")));

		Journalpost journalpost = parseJournalpost(journalpostQuery());
		assertThat(journalpost.getTema(), is(Tema.PEN));
		assertThat(journalpost.getSak().getArkivsaksnummer(), is("100000000"));
		assertThat(journalpost.getSak().getFagsakId(), nullValue());
		assertThat(journalpost.getSak().getFagsaksystem(), is("FS22"));
		assertThat(journalpost.getBruker().getId(), nullValue());
	}

	@Test
	void shouldQueryJournalpostAndFallbackToUkjentTemaWhenNoSakOrJournalpostTemaFound() throws Exception {
		abacPermit();

		stubFor(get("/hentjournalsakinfo/hentjournalpost/" + JOURNALPOST_ID)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/hentjournalpost_not_bid-null-temas.json")));

		Journalpost journalpost = parseJournalpost(journalpostQuery());
		assertThat(journalpost.getTema(), is(Tema.UKJ));
	}

	@Test
	void shouldQueryJournalpostByJournalpostIdWhenAllExceptPep1AccessPermitWithSakOrgnr() throws Exception {
		abacPermitExceptPep1();

		stubFor(get("/hentjournalsakinfo/hentjournalpost/" + JOURNALPOST_ID)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/hentjournalpost_orgnr-happy.json")));

		Journalpost journalpost = parseJournalpost(journalpostQuery());
		assertThat(journalpost, notNullValue());
	}

	@Test
	void shouldReturnNullJournalpostWhenDenyOnPep1g() throws Exception {
		abacDenyPep1g();
		stubFor(get("/hentjournalsakinfo/hentjournalpost/" + JOURNALPOST_ID)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/hentjournalpost_not_bid-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = journalpostQuery();
		assertErrorWithMessage(responseEntity, PEP1G_ERRORMESSAGE);
	}

	@Test
	void shouldReturnNullJournalpostWhenDenyOnPep2() throws Exception {
		abacDenyPep2();
		stubFor(get("/hentjournalsakinfo/hentjournalpost/" + JOURNALPOST_ID)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/hentjournalpost_far-happy.json")));
		stubFor(get("/gsak/" + GSAK_ID)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakBySaksId_far-happy.json")));
		stubFor(get("/bidrag/" + BIDRAG_SAK_ID)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("bidrag/bidragsak-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = journalpostQuery();
		assertErrorWithMessage(responseEntity, PEP2_ERRORMESSAGE);
	}

	@Test
	void shouldReturnSaksbehandlerTilgangFalseWhenDenyOnPep2d() throws Exception {
		abacDenyPep2dSkipPep2();
		stubFor(get("/hentjournalsakinfo/hentjournalpost/" + JOURNALPOST_ID)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/hentjournalpost_not_bid-happy.json")));
		stubFor(get("/gsak/" + GSAK_ID)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakBySaksId_not_bid-happy.json")));

		Journalpost journalpost = parseJournalpost(journalpostQuery());
		DokumentInfo dokumentInfo1 = journalpost.getDokumenter().get(0);
		assertFalse(dokumentInfo1.getDokumentvarianter().get(0).isSaksbehandlerHarTilgang());
	}

	@Test
	void shouldReturnNullJournalpostWhenDenyOnPep3() throws Exception {
		abacDenyPep3();
		stubFor(get("/hentjournalsakinfo/hentjournalpost/" + JOURNALPOST_ID)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/hentjournalpost_far-happy.json")));
		stubFor(get("/bidrag/" + BIDRAG_SAK_ID)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("bidrag/bidragsak-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = journalpostQuery();
		assertErrorWithMessage(responseEntity, PEP3_ERRORMESSAGE);
	}

	@Test
	void shouldReturnNullJournalpostWhenDenyOnPep4() throws Exception {
		abacDenyPep4SkipPep2Pep3();
		stubFor(get("/hentjournalsakinfo/hentjournalpost/" + JOURNALPOST_ID)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/hentjournalpost_jp_pol_skjerming-happy.json")));
		stubFor(get("/gsak/" + GSAK_ID)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakBySaksId_not_bid-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = journalpostQuery();
		assertErrorWithMessage(responseEntity, PEP4_ERRORMESSAGE);
	}

	@Test
	void shouldReturnJournalpostWithOneFilteredDokumentInfoWhenDenyOnPep5() throws Exception {
		abacDenyPep5SkipPep2Pep3Pep4();
		stubFor(get("/hentjournalsakinfo/hentjournalpost/" + JOURNALPOST_ID)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/hentjournalpost_dokumentinfo_pol_skjerming-happy.json")));

		Journalpost journalpost = parseJournalpost(journalpostQuery());
		assertThat(journalpost.getDokumenter(), hasSize(1));
	}

	@Test
	void shouldReturnSaksbehandlerTilgangFalseOnVariantWithDenyOnPep6d() throws Exception {
		abacDenyPep6dSkipPep2Pep3Pep4Pep5();
		stubFor(get("/hentjournalsakinfo/hentjournalpost/" + JOURNALPOST_ID)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/hentjournalpost_variant_pol_skjerming-happy.json")));

		Journalpost journalpost = parseJournalpost(journalpostQuery());
		DokumentInfo dokumentInfo1 = journalpost.getDokumenter().get(0);
		assertFalse(dokumentInfo1.getDokumentvarianter().get(0).isSaksbehandlerHarTilgang());
	}

	private void assertErrorWithMessage(ResponseEntity<LinkedHashMap> responseEntity, String expectedErrorMessage) throws Exception {
		Map<String, Object> data = (Map<String, Object>) responseEntity.getBody().get("data");
		assertThat(data.get("journalpost"), nullValue());
		assertErrorMessage(responseEntity, expectedErrorMessage);
	}

	private void assertErrorMessage(ResponseEntity<LinkedHashMap> responseEntity, String expectedErrorMessage) {
		assertThat(((LinkedHashMap) ((ArrayList) responseEntity.getBody().get("errors")).get(0)).get("message"), is(expectedErrorMessage));
	}

	private ResponseEntity<LinkedHashMap> journalpostQuery() throws IOException, URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("journalpost/journalpost.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, LinkedHashMap.class);
	}

	private Journalpost parseJournalpost(ResponseEntity<LinkedHashMap> responseEntity) {
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		return OBJECT_MAPPER.convertValue(responseEntityData.get("journalpost"), Journalpost.class);
	}
}
