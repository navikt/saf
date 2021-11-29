package no.nav.saf.endpoints.tilknyttedejournalposter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Journalposttype;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.visningsmodell.DokumentInfo;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.endpoints.AbstractItest;
import no.nav.saf.endpoints.graphql.GraphQLRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.Sakstype.GENERELL_SAK;
import static no.nav.saf.domain.kode.Datotype.DATO_EKSPEDERT;
import static no.nav.saf.domain.kode.Dokumentstatus.FERDIGSTILT;
import static no.nav.saf.domain.kode.Kanal.SDP;
import static no.nav.saf.domain.kode.Tema.FOR;
import static no.nav.saf.domain.kode.Variantformat.ARKIV;
import static no.nav.saf.domain.visningsmodell.BrukerIdType.AKTOERID;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class TilknyttedeJournalposterIT extends AbstractItest {
	private final String JOURNALPOST_ID = "400000000";
	private final String DOKUMENT_INFO_ID = "500000000";
	private final String GSAK_ID = "100000000";
	private final String BIDRAG_SAK_ID = "abc123";

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	static {
		OBJECT_MAPPER.registerModule(new JavaTimeModule());
	}

	@Test
	void shouldReturnTilknyttedeJournalposter() throws Exception {
		abacPermit();
		stubHappyPdl();
		this.stubHapptTilknyttedejournalposter();

		List<Journalpost> tilknyttedeJournalposter = parseJournalpost(tilknyttedeJournalposterGjenbrukQuery());
		assertThat(tilknyttedeJournalposter, hasSize(1));
		Journalpost journalpost = tilknyttedeJournalposter.get(0);

		assertThat(journalpost.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(journalpost.getTittel(), is("En søknad om noe"));
		assertThat(journalpost.getJournalposttype(), is(Journalposttype.U));
		assertThat(journalpost.getJournalstatus(), is(Journalstatus.FERDIGSTILT));
		assertThat(journalpost.getTema(), is(FOR));
		assertThat(journalpost.getTemanavn(), is(FOR.getTemanavn()));
		assertThat(journalpost.getBehandlingstema(), is("sok1"));
		assertThat(journalpost.getBehandlingstemanavn(), is("En viktig søknad"));
		assertThat(journalpost.getSak().getArkivsaksnummer(), is("100000000"));
		assertThat(journalpost.getSak().getArkivsaksystem(), is(Arkivsakssystem.GSAK));
		assertThat(journalpost.getSak().getTema(), is(FOR));
		assertThat(journalpost.getSak().getFagsakId(), is("abc123"));
		assertThat(journalpost.getSak().getFagsaksystem(), is("FS22"));
		assertThat(journalpost.getSak().getDatoOpprettet(), notNullValue());
		assertThat(journalpost.getSak().getArkivsaksnummer(), is("100000000"));
		assertThat(journalpost.getSak().getSakstype(), is(GENERELL_SAK));
		assertThat(journalpost.getBruker().getId(), is("1912374211459"));
		assertThat(journalpost.getBruker().getType(), is(AKTOERID));
		assertThat(journalpost.getAvsenderMottaker().getId(), is("11111111111"));
		assertThat(journalpost.getAvsenderMottaker().getNavn(), is("Bjarne Betjent"));
		assertThat(journalpost.getAvsenderMottaker().getLand(), is("NO"));
		assertTrue(journalpost.getAvsenderMottaker().isErLikBruker());
		assertThat(journalpost.getAvsenderMottakerId(), is("11111111111"));
		assertThat(journalpost.getAvsenderMottakerNavn(), is("Bjarne Betjent"));
		assertThat(journalpost.getAvsenderMottakerLand(), is("NO"));
		assertThat(journalpost.getJournalforendeEnhet(), is("2990"));
		assertThat(journalpost.getJournalfoerendeEnhet(), is("2990"));
		assertThat(journalpost.getJournalfortAvNavn(), is("Max Mekker"));
		assertThat(journalpost.getOpprettetAvNavn(), is("Max Mekker"));
		assertThat(journalpost.getKanal(), is(SDP));
		assertThat(journalpost.getKanalnavn(), is(SDP.getKanalnavn()));
		assertThat(journalpost.getDatoOpprettet(), notNullValue());
		assertThat(journalpost.getRelevanteDatoer().get(0).getDatotype(), is(DATO_EKSPEDERT));
		assertThat(journalpost.getTilleggsopplysninger().get(0).getNokkel(), is("min_nokkel"));
		assertThat(journalpost.getTilleggsopplysninger().get(0).getVerdi(), is("min_verdi"));
		DokumentInfo dokumentInfo1 = journalpost.getDokumenter().get(0);
		assertThat(dokumentInfo1.getDokumentInfoId(), is("500000000"));
		assertThat(dokumentInfo1.getTittel(), is("Dokument1"));
		assertThat(dokumentInfo1.getBrevkode(), is("for123"));
		assertThat(dokumentInfo1.getDokumentstatus(), is(FERDIGSTILT));
		assertThat(dokumentInfo1.getOriginalJournalpostId(), is(JOURNALPOST_ID));
		assertThat(dokumentInfo1.getLogiskeVedlegg().get(0).getTittel(), is("Hei"));
		assertThat(dokumentInfo1.getDokumentvarianter().get(0).getVariantformat(), is(ARKIV));
		assertTrue(dokumentInfo1.getDokumentvarianter().get(0).isSaksbehandlerHarTilgang());
	}

	@Test
	void shouldReturnNoJournalpostsWhenDenyOnPep1g() throws Exception {
		abacDenyPep1g();
		stubHappyPdl();
		this.stubHapptTilknyttedejournalposter();

		List<Journalpost> tilknyttedeJournalposter = parseJournalpost(tilknyttedeJournalposterGjenbrukQuery());
		assertThat(tilknyttedeJournalposter, hasSize(0));
	}

	@Test
	void shouldReturnNoJournalpostsWhenDenyOnPep2() throws Exception {
		abacDenyPep2();
		stubHappyPdl();
		this.stubHappyTilknyttedejournalposterWithBody("hentjournalsakinfo/tilknyttedejournalposter_far-happy.json");

		List<Journalpost> tilknyttedeJournalposter = parseJournalpost(tilknyttedeJournalposterGjenbrukQuery());
		assertThat(tilknyttedeJournalposter, hasSize(0));
	}

	@Test
	void shouldReturnSaksbehandlerTilgangFalseWhenDenyOnPep2d() throws Exception {
		abacDenyPep2dSkipPep2();
		stubHappyPdl();
		this.stubHapptTilknyttedejournalposter();

		List<Journalpost> tilknyttedeJournalposter = parseJournalpost(tilknyttedeJournalposterGjenbrukQuery());
		DokumentInfo dokumentInfo = tilknyttedeJournalposter.get(0).getDokumenter().get(0);
		assertFalse(dokumentInfo.getDokumentvarianter().get(0).isSaksbehandlerHarTilgang());
	}

	@Test
	void shouldReturnNoJournalpostsWhenDenyOnPep3() throws Exception {
		abacDenyPep3SkipPep2();
		stubHappyPdl();
		this.stubHappyTilknyttedejournalposterWithBody("hentjournalsakinfo/tilknyttedejournalposter_bid-happy.json");
		this.stubHappyGsakWithBody("gsak/gsak-sakBySaksId-happy.json");
		stubFor(get("/bidrag/" + BIDRAG_SAK_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("bidrag/bidragsak-happy.json")));

		List<Journalpost> tilknyttedeJournalposter = parseJournalpost(tilknyttedeJournalposterGjenbrukQuery());
		assertThat(tilknyttedeJournalposter, hasSize(0));
	}

	@Test
	void shouldReturnNoJournalpostWhenDenyOnPep4() throws Exception {
		abacDenyPep4SkipPep2Pep3();
		stubHappyPdl();
		this.stubHappyTilknyttedejournalposterWithBody("hentjournalsakinfo/tilknyttedejournalposter_jp_pol_skjerming-happy.json");

		List<Journalpost> tilknyttedeJournalposter = parseJournalpost(tilknyttedeJournalposterGjenbrukQuery());
		assertThat(tilknyttedeJournalposter, hasSize(0));
	}

	@Test
	void shouldReturnJournalpostWithOneFilteredDokumentInfoWhenDenyOnPep5() throws Exception {
		abacDenyPep5SkipPep2Pep3Pep4();
		stubHappyPdl();
		this.stubHappyTilknyttedejournalposterWithBody("hentjournalsakinfo/tilknyttedejournalposter_dokumentinfo_pol_skjerming-happy.json");

		List<Journalpost> tilknyttedeJournalposter = parseJournalpost(tilknyttedeJournalposterGjenbrukQuery());
		assertThat(tilknyttedeJournalposter.get(0).getDokumenter(), hasSize(1));
	}

	@Test
	void shouldReturnSaksbehandlerTilgangFalseOnVariantWithDenyOnPep6d() throws Exception {
		abacDenyPep6dSkipPep2Pep3Pep4Pep5();
		stubHappyPdl();
		this.stubHappyTilknyttedejournalposterWithBody("hentjournalsakinfo/tilknyttedejournalposter_variant_pol_skjerming-happy.json");
		this.stubHappyGsakWithBody("gsak/gsak-sakBySaksId_not_bid-happy.json");

		List<Journalpost> tilknyttedeJournalposter = parseJournalpost(tilknyttedeJournalposterGjenbrukQuery());
		DokumentInfo dokumentInfo = tilknyttedeJournalposter.get(0).getDokumenter().get(0);
		assertFalse(dokumentInfo.getDokumentvarianter().get(0).isSaksbehandlerHarTilgang());
	}

	private void stubHappyGsakWithBody(String body) {
		stubFor(get("/gsak/" + GSAK_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile(body)));
	}

	private void stubHappyTilknyttedejournalposterWithBody(String body) {
		stubFor(get("/hentjournalsakinfo/tilknyttedejournalposter/" + DOKUMENT_INFO_ID + "/GJENBRUK")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile(body)));
	}

	private void stubHapptTilknyttedejournalposter() {
		stubFor(get("/hentjournalsakinfo/tilknyttedejournalposter/" + DOKUMENT_INFO_ID + "/GJENBRUK")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/tilknyttedejournalposter_not_bid-happy.json")));
	}

	private ResponseEntity<LinkedHashMap> tilknyttedeJournalposterGjenbrukQuery() throws URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("tilknyttedejournalposter/tilknyttedejournalpostergjenbruk.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, LinkedHashMap.class);
	}

	private List<Journalpost> parseJournalpost(ResponseEntity<LinkedHashMap> responseEntity) {
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		return OBJECT_MAPPER.convertValue(responseEntityData.get("tilknyttedeJournalposter"), new TypeReference<>() {
		});
	}
}
