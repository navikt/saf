package no.nav.saf.endpoints.tilknyttedejournalposter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Journalposttype;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.visningsmodell.AvsenderMottaker;
import no.nav.saf.domain.visningsmodell.DokumentInfo;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.endpoints.AbstractItest;
import no.nav.saf.endpoints.graphql.GraphQLRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostMapper.SKJULT_TITTEL;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.Sakstype.FAGSAK;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.Sakstype.GENERELL_SAK;
import static no.nav.saf.domain.kode.Datotype.DATO_DOKUMENT;
import static no.nav.saf.domain.kode.Datotype.DATO_EKSPEDERT;
import static no.nav.saf.domain.kode.Dokumentstatus.FERDIGSTILT;
import static no.nav.saf.domain.kode.Kanal.SDP;
import static no.nav.saf.domain.kode.Kanal.SENTRAL_UTSKRIFT;
import static no.nav.saf.domain.kode.Tema.FOR;
import static no.nav.saf.domain.kode.Tema.UFO;
import static no.nav.saf.domain.kode.Variantformat.ARKIV;
import static no.nav.saf.domain.kode.Variantformat.PRODUKSJON;
import static no.nav.saf.domain.visningsmodell.BrukerIdType.AKTOERID;
import static no.nav.saf.domain.visningsmodell.BrukerIdType.FNR;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class TilknyttedeJournalposterIT extends AbstractItest {
	private static final String JOURNALPOST_ID = "400000000";
	private static final String DOKUMENT_INFO_ID = "500000000";
	private static final String SAK_ID = "100000000";
	private static final String BIDRAG_SAK_ID = "abc123";

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

	@BeforeEach
	void setUp() {
		setupHappyPathAzureToken();
	}

	@Test
	void shouldReturnTilknyttedeJournalposter() throws Exception {
		abacPermit();
		stubFor(get("/dokarkiv/tilknyttedeJournalposter/gjenbruk/dokumentInfoId/" + DOKUMENT_INFO_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/tilknyttetjournalpost/tilknyttedejournalposter-not-bid-happy.json")));
		stubPdl();

		List<Journalpost> tilknyttedeJournalposter = parseJournalpost(tilknyttedeJournalposterGjenbrukQuery());
		assertThat(tilknyttedeJournalposter, hasSize(1));
		Journalpost journalpost = tilknyttedeJournalposter.getFirst();

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
		assertThat(journalpost.getSak().getDatoOpprettet(), is(LocalDateTime.parse("2018-07-17T13:49:01", ISO_LOCAL_DATE_TIME)));
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
		assertThat(dokumentInfo1.getLogiskeVedlegg().get(0).getTittel(), is("Søknadsinfo"));
		assertThat(dokumentInfo1.getDokumentvarianter().get(0).getVariantformat(), is(ARKIV));
		assertTrue(dokumentInfo1.getDokumentvarianter().get(0).isSaksbehandlerHarTilgang());
	}

	@Test
	void shouldReturnTilknyttedeJournalposterPsak() throws Exception {
		abacPermit();
		stubPensjonBrukerForSak();
		stubPensjonSakSammendrag();
		stubFor(get("/dokarkiv/tilknyttedeJournalposter/gjenbruk/dokumentInfoId/" + DOKUMENT_INFO_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/tilknyttetjournalpost/tilknyttedejournalposter-psak-utgaaende-happy.json")));
		stubPdl();

		List<Journalpost> tilknyttedeJournalposter = parseJournalpost(tilknyttedeJournalposterGjenbrukQuery());
		assertThat(tilknyttedeJournalposter, hasSize(1));
		Journalpost journalpost = tilknyttedeJournalposter.getFirst();

		assertThat(journalpost.getJournalpostId(), is(JOURNALPOST_ID));
		assertThat(journalpost.getTittel(), is("Vedtak – omregning fra uføretrygd til alderspensjon"));
		assertThat(journalpost.getJournalposttype(), is(Journalposttype.U));
		assertThat(journalpost.getJournalstatus(), is(Journalstatus.FERDIGSTILT));
		assertThat(journalpost.getTema(), is(UFO));
		assertThat(journalpost.getTemanavn(), is(UFO.getTemanavn()));
		assertThat(journalpost.getBehandlingstema(), nullValue());
		assertThat(journalpost.getBehandlingstemanavn(), nullValue());
		assertThat(journalpost.getSak().getArkivsaksnummer(), is("21998969"));
		assertThat(journalpost.getSak().getArkivsaksystem(), is(Arkivsakssystem.PSAK));
		assertThat(journalpost.getSak().getTema(), is(UFO));
		assertThat(journalpost.getSak().getFagsakId(), is("21998969"));
		assertThat(journalpost.getSak().getFagsaksystem(), is("PP01"));
		assertThat(journalpost.getSak().getDatoOpprettet(), is(LocalDateTime.parse("2015-06-01T00:00:00", ISO_LOCAL_DATE_TIME)));
		assertThat(journalpost.getSak().getSakstype(), is(FAGSAK));
		assertThat(journalpost.getBruker().getId(), is("1912374211459"));
		assertThat(journalpost.getBruker().getType(), is(AKTOERID));
		assertThat(journalpost.getAvsenderMottaker().getId(), is("10525619171"));
		assertThat(journalpost.getAvsenderMottaker().getNavn(), is("FISKEBOLLE FROM"));
		assertThat(journalpost.getAvsenderMottaker().getLand(), is("NO"));
		assertTrue(journalpost.getAvsenderMottaker().isErLikBruker());
		assertThat(journalpost.getAvsenderMottakerId(), is("10525619171"));
		assertThat(journalpost.getAvsenderMottakerNavn(), is("FISKEBOLLE FROM"));
		assertThat(journalpost.getAvsenderMottakerLand(), is("NO"));
		assertThat(journalpost.getJournalforendeEnhet(), is("4808"));
		assertThat(journalpost.getJournalfoerendeEnhet(), is("4808"));
		assertThat(journalpost.getJournalfortAvNavn(), is("PESYS_MASKINELL"));
		assertThat(journalpost.getOpprettetAvNavn(), is("PESYS_MASKINELL"));
		assertThat(journalpost.getKanal(), is(SENTRAL_UTSKRIFT));
		assertThat(journalpost.getKanalnavn(), is(SENTRAL_UTSKRIFT.getKanalnavn()));
		assertThat(journalpost.getDatoOpprettet(), notNullValue());
		assertThat(journalpost.getRelevanteDatoer().get(0).getDatotype(), is(DATO_DOKUMENT));
		assertThat(journalpost.getTilleggsopplysninger(), hasSize(0));
		assertThat(journalpost.getDokumenter(), hasSize(4));

		DokumentInfo dokumentInfo1 = journalpost.getDokumenter().getFirst();
		assertThat(dokumentInfo1.getDokumentInfoId(), is("500000000"));
		assertThat(dokumentInfo1.getTittel(), is("Vedtak – omregning fra uføretrygd til alderspensjon"));
		assertThat(dokumentInfo1.getBrevkode(), is("000086"));
		assertThat(dokumentInfo1.getDokumentstatus(), is(FERDIGSTILT));
		assertThat(dokumentInfo1.getOriginalJournalpostId(), is(JOURNALPOST_ID));
		assertThat(dokumentInfo1.getLogiskeVedlegg(), hasSize(0));
		assertThat(dokumentInfo1.getDokumentvarianter(), hasSize(2));
		assertThat(dokumentInfo1.getDokumentvarianter().get(0).getVariantformat(), is(ARKIV));
		assertTrue(dokumentInfo1.getDokumentvarianter().get(0).isSaksbehandlerHarTilgang());
		assertThat(dokumentInfo1.getDokumentvarianter().get(1).getVariantformat(), is(PRODUKSJON));
		assertTrue(dokumentInfo1.getDokumentvarianter().get(1).isSaksbehandlerHarTilgang());
	}

	@Test
	void shouldReturnTilknyttedeJournalposterWithEmptyAvsendermottakerWhenOriginalAvsendermottakerIsNull() throws Exception {
		abacPermit();
		stubFor(get("/dokarkiv/tilknyttedeJournalposter/gjenbruk/dokumentInfoId/" + DOKUMENT_INFO_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/tilknyttetjournalpost/tilknyttedejournalposter-no-avsendermottaker-happy.json")));
		stubPdl();

		List<Journalpost> tilknyttedeJournalposter = parseJournalpost(tilknyttedeJournalposterGjenbrukQuery());
		assertThat(tilknyttedeJournalposter, hasSize(1));
		Journalpost journalpost = tilknyttedeJournalposter.getFirst();

		assertThat(journalpost.getJournalpostId(), is(JOURNALPOST_ID));

		assertThat(journalpost.getAvsenderMottaker(), notNullValue(AvsenderMottaker.class));
		assertThat(journalpost.getAvsenderMottaker().getId(), nullValue());
		assertThat(journalpost.getAvsenderMottaker().getNavn(), nullValue());
		assertThat(journalpost.getAvsenderMottaker().getLand(), nullValue());
		assertThat(journalpost.getAvsenderMottaker().isErLikBruker(), is(false));
		assertThat(journalpost.getAvsenderMottakerId(), nullValue());
		assertThat(journalpost.getAvsenderMottakerNavn(), nullValue());
		assertThat(journalpost.getAvsenderMottakerLand(), nullValue());
	}

	@Test
	void shouldReturnNoJournalpostsWhenDenyOnPep1g() throws Exception {
		abacDenyPep1g();
		stubFor(get("/dokarkiv/tilknyttedeJournalposter/gjenbruk/dokumentInfoId/" + DOKUMENT_INFO_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/tilknyttetjournalpost/tilknyttedejournalposter-not-bid-happy.json")));
		stubPdl();

		List<Journalpost> tilknyttedeJournalposter = parseJournalpost(tilknyttedeJournalposterGjenbrukQuery());
		assertThat(tilknyttedeJournalposter, hasSize(0));
	}

	@Test
	void shouldReturnNoJournalpostsWhenDenyOnPep2() throws Exception {
		abacDenyPep2();
		stubFor(get("/dokarkiv/tilknyttedeJournalposter/gjenbruk/dokumentInfoId/" + DOKUMENT_INFO_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/tilknyttetjournalpost/tilknyttedejournalposter-far-happy.json")));
		stubPdl();

		List<Journalpost> tilknyttedeJournalposter = parseJournalpost(tilknyttedeJournalposterGjenbrukQuery());
		assertThat(tilknyttedeJournalposter, hasSize(0));
	}

	@Test
	void shouldReturnSaksbehandlerTilgangFalseWhenDenyOnPep2d() throws Exception {
		abacDenyPep2dSkipPep2();
		stubFor(get("/dokarkiv/tilknyttedeJournalposter/gjenbruk/dokumentInfoId/" + DOKUMENT_INFO_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/tilknyttetjournalpost/tilknyttedejournalposter-not-bid-happy.json")));
		stubPdl();

		List<Journalpost> tilknyttedeJournalposter = parseJournalpost(tilknyttedeJournalposterGjenbrukQuery());
		Journalpost journalpost = tilknyttedeJournalposter.get(0);
		assertThat(journalpost.getTittel(), is(SKJULT_TITTEL));
		DokumentInfo dokumentInfo = journalpost.getDokumenter().get(0);
		assertThat(dokumentInfo.getTittel(), is(SKJULT_TITTEL));
		assertFalse(dokumentInfo.getDokumentvarianter().get(0).isSaksbehandlerHarTilgang());
		assertThat(dokumentInfo.getLogiskeVedlegg().get(0).getTittel(), is(SKJULT_TITTEL));
	}

	@Test
	void shouldReturnNoJournalpostsWhenDenyOnPep3() throws Exception {
		abacDenyPep3SkipPep2();
		stubFor(get("/dokarkiv/tilknyttedeJournalposter/gjenbruk/dokumentInfoId/" + DOKUMENT_INFO_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/tilknyttetjournalpost/tilknyttedejournalposter-bid-happy.json")));
		stubFor(get("/sak/" + SAK_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("sak/sak-sakBySaksId-happy.json")));
		stubFor(get("/bidrag/" + BIDRAG_SAK_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("bidrag/bidragsak-happy.json")));
		stubPdl();

		List<Journalpost> tilknyttedeJournalposter = parseJournalpost(tilknyttedeJournalposterGjenbrukQuery());
		assertThat(tilknyttedeJournalposter, hasSize(0));
	}

	@Test
	void shouldReturnNoJournalpostWhenDenyOnPep4() throws Exception {
		abacDenyPep4SkipPep2Pep3();
		stubFor(get("/dokarkiv/tilknyttedeJournalposter/gjenbruk/dokumentInfoId/" + DOKUMENT_INFO_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/tilknyttetjournalpost/tilknyttedejournalposter-skjerming-jp-pol-happy.json")));
		stubPdl();

		List<Journalpost> tilknyttedeJournalposter = parseJournalpost(tilknyttedeJournalposterGjenbrukQuery());
		assertThat(tilknyttedeJournalposter, hasSize(0));
	}

	@Test
	void shouldReturnJournalpostWithOneFilteredDokumentInfoWhenDenyOnPep5() throws Exception {
		abacDenyPep5SkipPep2Pep3Pep4();
		stubFor(get("/dokarkiv/tilknyttedeJournalposter/gjenbruk/dokumentInfoId/" + DOKUMENT_INFO_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/tilknyttetjournalpost/tilknyttedejournalposter-skjerming-dokumentinfo-pol-happy.json")));
		stubPdl();

		List<Journalpost> tilknyttedeJournalposter = parseJournalpost(tilknyttedeJournalposterGjenbrukQuery());
		assertThat(tilknyttedeJournalposter.get(0).getDokumenter(), hasSize(1));
		assertThat(tilknyttedeJournalposter.get(0).getDokumenter().getFirst().getDokumentInfoId(), is("500000001"));
	}

	@Test
	void shouldReturnSaksbehandlerTilgangFalseOnVariantWithDenyOnPep6d() throws Exception {
		abacDenyPep6dSkipPep2Pep3Pep4Pep5();
		stubFor(get("/dokarkiv/tilknyttedeJournalposter/gjenbruk/dokumentInfoId/" + DOKUMENT_INFO_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/tilknyttetjournalpost/tilknyttedejournalposter-skjerming-variant-pol-happy.json")));
		stubFor(get("/sak/" + SAK_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("sak/sak-sakBySaksId_not_bid-happy.json")));
		stubPdl();

		List<Journalpost> tilknyttedeJournalposter = parseJournalpost(tilknyttedeJournalposterGjenbrukQuery());
		DokumentInfo dokumentInfo = tilknyttedeJournalposter.get(0).getDokumenter().get(0);
		assertFalse(dokumentInfo.getDokumentvarianter().get(0).isSaksbehandlerHarTilgang());
	}

	@Test
	void shouldReturnTilknyttedeJournalposterWithoutSakOrBruker() throws Exception {
		abacPermit();
		stubFor(get("/dokarkiv/tilknyttedeJournalposter/gjenbruk/dokumentInfoId/" + DOKUMENT_INFO_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/tilknyttetjournalpost/tilknyttedejournalposter-no-sak-no-bruker-happy.json")));

		List<Journalpost> tilknyttedeJournalposter = parseJournalpost(tilknyttedeJournalposterGjenbrukQuery());

		assertThat(tilknyttedeJournalposter, hasSize(1));
		assertThat(tilknyttedeJournalposter.getFirst().getSak(), nullValue());
		assertThat(tilknyttedeJournalposter.getFirst().getBruker(), nullValue());
	}

	@Test
	void shouldReturnTilknyttedeJournalposterWhenBrukerNotFoundInPDL() throws Exception {
		abacPermit();
		stubPdl("pdl-person-ikke-funnet.json");
		stubFor(get("/dokarkiv/tilknyttedeJournalposter/gjenbruk/dokumentInfoId/" + DOKUMENT_INFO_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/tilknyttetjournalpost/tilknyttedejournalposter-no-sak-happy.json")));

		var response = tilknyttedeJournalposterGjenbrukQuery();

		assertThat(response.getBody().get("errors"), nullValue());

		List<Journalpost> tilknyttedeJournalposter = parseJournalpost(response);

		assertThat(tilknyttedeJournalposter, hasSize(1));
		assertThat(tilknyttedeJournalposter.getFirst().getSak(), nullValue());
		assertThat(tilknyttedeJournalposter.getFirst().getBruker().getId(), is("11111111111"));
		assertThat(tilknyttedeJournalposter.getFirst().getBruker().getType(), is(FNR));
	}

	private ResponseEntity<LinkedHashMap> tilknyttedeJournalposterGjenbrukQuery() throws URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("tilknyttedejournalposter/tilknyttedejournalpostergjenbruk.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, LinkedHashMap.class);
	}

	private List<Journalpost> parseJournalpost(ResponseEntity<LinkedHashMap> responseEntity) {
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");

		return responseEntityData == null ? null : OBJECT_MAPPER.convertValue(responseEntityData.get("tilknyttedeJournalposter"), new TypeReference<List<Journalpost>>() {
		});
	}
}
