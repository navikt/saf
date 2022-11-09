package no.nav.saf.endpoints.journalpost;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.Sakstype;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Journalposttype;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.visningsmodell.BrukerIdType;
import no.nav.saf.domain.visningsmodell.DokumentInfo;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.endpoints.AbstractItest;
import no.nav.saf.endpoints.graphql.GraphQLRequest;
import no.nav.saf.endpoints.graphql.GraphQLResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;

import java.net.URI;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.Sakstype.GENERELL_SAK;
import static no.nav.saf.domain.kode.Datotype.DATO_EKSPEDERT;
import static no.nav.saf.domain.kode.Dokumentstatus.FERDIGSTILT;
import static no.nav.saf.domain.kode.Kanal.SDP;
import static no.nav.saf.domain.kode.Tema.PEN;
import static no.nav.saf.domain.kode.Tema.UKJ;
import static no.nav.saf.domain.kode.Variantformat.ARKIV;
import static no.nav.saf.graphql.ErrorCode.BAD_REQUEST;
import static no.nav.saf.graphql.ErrorCode.FORBIDDEN;
import static no.nav.saf.graphql.ErrorCode.NOT_FOUND;
import static no.nav.saf.graphql.ErrorCode.SERVER_ERROR;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class JournalpostIT extends AbstractItest {
	private final String JOURNALPOST_ID = "400000000";
	private final String GSAK_ID = "100000000";

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	static {
		OBJECT_MAPPER.registerModule(new JavaTimeModule());
	}

	@Test
	void shouldQueryJournalpostByJournalpostIdWhenAllAccessPermit() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentjournalpost/" + JOURNALPOST_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
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
		assertThat(journalpost.getSak().getFagsaksystem(), is("K9"));
		assertThat(journalpost.getSak().getDatoOpprettet(), notNullValue());
		assertThat(journalpost.getSak().getSakstype(), is(Sakstype.FAGSAK));
		assertThat(journalpost.getSak().getTema(), is(Tema.FOR));
		assertThat(journalpost.getBruker().getId(), is("1900000000000"));
		assertThat(journalpost.getBruker().getType(), is(BrukerIdType.AKTOERID));
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
		assertThat(journalpost.getEksternReferanseId(), is("KANAL REFERANSE ID"));
		DokumentInfo dokumentInfo1 = journalpost.getDokumenter().get(0);
		assertThat(dokumentInfo1.getDokumentInfoId(), is("500000000"));
		assertThat(dokumentInfo1.getTittel(), is("Dokument1"));
		assertThat(dokumentInfo1.getBrevkode(), is("for123"));
		assertThat(dokumentInfo1.getDokumentstatus(), is(FERDIGSTILT));
		assertThat(dokumentInfo1.getOriginalJournalpostId(), is(JOURNALPOST_ID));
		assertThat(dokumentInfo1.getLogiskeVedlegg().get(0).getTittel(), is("Hei"));
		assertThat(dokumentInfo1.getDokumentvarianter().get(0).getVariantformat(), is(ARKIV));
		assertTrue(dokumentInfo1.getDokumentvarianter().get(0).isSaksbehandlerHarTilgang());
		assertThat(dokumentInfo1.getDokumentvarianter().get(0).getFiltype(), is("PDF"));
		assertThat(dokumentInfo1.getDokumentvarianter().get(0).getFilnavn(), is("filNavn"));
		assertThat(dokumentInfo1.getDokumentvarianter().get(0).getFiluuid(), is("0c0bacf-c233-4a54-96fc-e205b79862d9"));
	}

	@Test
	void shouldQueryJournalpostWhenSakNotFound() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentjournalpost/" + JOURNALPOST_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/hentjournalpost_not_bid-null-user-and-sak-happy.json")));

		Journalpost journalpost = parseJournalpost(journalpostQuery());
		assertThat(journalpost.getTema(), is(PEN));
		assertThat(journalpost.getSak().getArkivsaksnummer(), is("100000000"));
		assertThat(journalpost.getSak().getFagsakId(), nullValue());
		assertThat(journalpost.getSak().getFagsaksystem(), is("FS22"));
		assertThat(journalpost.getSak().getSakstype(), is(GENERELL_SAK));
		assertThat(journalpost.getSak().getTema(), is(PEN));
		assertThat(journalpost.getBruker(), nullValue());
	}

	@Test
	void shouldQueryJournalpostAndFallbackToUkjentTemaWhenNoSakOrJournalpostTemaFound() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentjournalpost/" + JOURNALPOST_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/hentjournalpost_not_bid-null-temas.json")));

		Journalpost journalpost = parseJournalpost(journalpostQuery());
		assertThat(journalpost.getTema(), is(UKJ));
	}

	@Test
	void shouldQueryJournalpostByJournalpostIdWhenAllExceptPep1AccessPermitWithSakOrgnr() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentjournalpost/" + JOURNALPOST_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/hentjournalpost_orgnr-happy.json")));

		Journalpost journalpost = parseJournalpost(journalpostQuery());
		assertThat(journalpost, notNullValue());
	}

	@Test
	void shouldReturnNullJournalpostWhenDenyOnPep1g() {
		abacDenyPep1g();
		stubFor(get("/hentjournalsakinfo/hentjournalpost/" + JOURNALPOST_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/hentjournalpost_not_bid-happy.json")));

		GraphQLResponse graphQLResponse = journalpostQuery();
		assertErrorWithCode(graphQLResponse, FORBIDDEN.getText());
		assertErrorWithMessage(graphQLResponse, "ikke tilgang til ressurs tilhørende bruker som har kode 6/7, egen ansatt eller utenfor tillatt geografisk område");
	}

	@Test
	void shouldReturnNullJournalpostWhenDenyOnPep2() {
		abacDenyPep2();
		stubFor(get("/hentjournalsakinfo/hentjournalpost/" + JOURNALPOST_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/hentjournalpost_far-happy.json")));
		stubFor(get("/gsak/" + GSAK_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakBySaksId_far-happy.json")));

		GraphQLResponse graphQLResponse = journalpostQuery();
		assertErrorWithCode(graphQLResponse, FORBIDDEN.getText());
		assertErrorWithMessage(graphQLResponse, "ikke tilgang til tema ressurset tilhører eller på grunn av Forvaltningsloven § 19.");
	}

	@Test
	void shouldReturnSaksbehandlerTilgangFalseWhenDenyOnPep2d() {
		abacDenyPep2dSkipPep2();
		stubFor(get("/hentjournalsakinfo/hentjournalpost/" + JOURNALPOST_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/hentjournalpost_not_bid-happy.json")));
		stubFor(get("/gsak/" + GSAK_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakBySaksId_not_bid-happy.json")));

		Journalpost journalpost = parseJournalpost(journalpostQuery());
		DokumentInfo dokumentInfo = journalpost.getDokumenter().get(0);
		assertFalse(dokumentInfo.getDokumentvarianter().get(0).isSaksbehandlerHarTilgang());
	}

	@Test
	void shouldReturnNullJournalpostWhenDenyOnPep3() {
		abacDenyPep3SkipPep2();
		stubFor(get("/hentjournalsakinfo/hentjournalpost/" + JOURNALPOST_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/hentjournalpost_bid-happy.json")));
		stubFor(get("/bidrag/abc123").willReturn(aResponse().withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		GraphQLResponse graphQLResponse = journalpostQuery();
		assertErrorWithCode(graphQLResponse, FORBIDDEN.getText());
		assertErrorWithMessage(graphQLResponse, "ikke tilgang til ressurs der en av partene i bidragssaken har kode6/7 eller egen ansatt.");
	}

	@Test
	void shouldReturnNullJournalpostWhenDenyOnPep4() {
		abacDenyPep4SkipPep2Pep3();
		stubFor(get("/hentjournalsakinfo/hentjournalpost/" + JOURNALPOST_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/hentjournalpost_jp_pol_skjerming-happy.json")));
		stubFor(get("/gsak/" + GSAK_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("gsak/gsak-sakBySaksId_not_bid-happy.json")));

		GraphQLResponse graphQLResponse = journalpostQuery();
		assertErrorWithCode(graphQLResponse, FORBIDDEN.getText());
		assertErrorWithMessage(graphQLResponse, "ikke tilgang til ressurs på grunn av journalposten sin status.");
	}

	@Test
	void shouldReturnJournalpostWithOneFilteredDokumentInfoWhenDenyOnPep5() {
		abacDenyPep5SkipPep2Pep3Pep4();
		stubFor(get("/hentjournalsakinfo/hentjournalpost/" + JOURNALPOST_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/hentjournalpost_dokumentinfo_pol_skjerming-happy.json")));

		Journalpost journalpost = parseJournalpost(journalpostQuery());
		assertThat(journalpost.getDokumenter(), hasSize(1));
	}

	@Test
	void shouldReturnSaksbehandlerTilgangFalseOnVariantWithDenyOnPep6d() {
		abacDenyPep6dSkipPep2Pep3Pep4Pep5();
		stubFor(get("/hentjournalsakinfo/hentjournalpost/" + JOURNALPOST_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("hentjournalsakinfo/hentjournalpost_variant_pol_skjerming-happy.json")));

		Journalpost journalpost = parseJournalpost(journalpostQuery());
		DokumentInfo dokumentInfo1 = journalpost.getDokumenter().get(0);
		assertFalse(dokumentInfo1.getDokumentvarianter().get(0).isSaksbehandlerHarTilgang());
	}

	@Test
	void shouldReturnErrorCodeNotFoundWhenJournalpostNotFound() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentjournalpost/" + JOURNALPOST_ID)
				.willReturn(aResponse().withStatus(HttpStatus.NOT_FOUND.value())));

		assertErrorWithCode(journalpostQuery(), NOT_FOUND.getText());
	}

	@Test
	void shouldReturnErrorCodeServerErrorWhenJournalpostNotFound() {
		abacPermit();
		stubFor(get("/hentjournalsakinfo/hentjournalpost/" + JOURNALPOST_ID)
				.willReturn(aResponse().withStatus(HttpStatus.INTERNAL_SERVER_ERROR.value())));

		assertErrorWithCode(journalpostQuery(), SERVER_ERROR.getText());
	}

	@Test
	void shouldReturnErrorCodeBadRequestWhenJournalpostIdNotValid() {
		assertErrorWithCode(journalpostQuery("journalpost_invalid_journalpostid.query"), BAD_REQUEST.getText());
	}

	private void assertErrorWithCode(GraphQLResponse graphQLResponse, String errorCode) {
		assertThat(graphQLResponse.getData().get("journalpost"), nullValue());
		assertThat(graphQLResponse.getErrors().get(0).getExtensions().getCode(), is(errorCode));
	}

	private void assertErrorWithMessage(GraphQLResponse graphQLResponse, String expectedErrorMessage) {
		assertThat(graphQLResponse.getData().get("journalpost"), nullValue());
		assertThat(graphQLResponse.getErrors().get(0).getMessage(), containsString(expectedErrorMessage));
	}

	@SneakyThrows
	private GraphQLResponse journalpostQuery() {
		return journalpostQuery("journalpost.query");
	}

	@SneakyThrows
	private GraphQLResponse journalpostQuery(String queryFile) {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("journalpost/" + queryFile), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, GraphQLResponse.class).getBody();
	}

	private Journalpost parseJournalpost(GraphQLResponse graphQLResponse) {
		return OBJECT_MAPPER.convertValue(graphQLResponse.getData().get("journalpost"), Journalpost.class);
	}
}
