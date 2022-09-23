package no.nav.saf.endpoints.saker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.saf.domain.visningsmodell.Sak;
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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.Sakstype.FAGSAK;
import static no.nav.saf.domain.kode.Arkivsakssystem.GSAK;
import static no.nav.saf.domain.kode.Arkivsakssystem.PSAK;
import static no.nav.saf.domain.kode.Tema.BID;
import static no.nav.saf.domain.kode.Tema.UFO;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class SakIT extends AbstractItest {

	private static final String AKTOER_ID = "1912374211459";
	private static final ObjectMapper objectMapper = new ObjectMapper();

	static {
		objectMapper.registerModule(new JavaTimeModule());
	}

	@BeforeEach
	void setup() {
		setupHappyPathRestSTS();
		setupHappyPathAzureToken();
	}

	@Test
	void shouldRemoveSakDuplicates() {
		abacPermit();
		stubFor(post("/pdl")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/hentPdlDataForIdent-happy.json")));
		stubFor(get("/gsak?aktoerId=" + AKTOER_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("gsak/gsak-sakerBySaksId-happy-duplicates.json")));
		stubFor(get("/pen/springapi/sak/sammendrag")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-duplicates.json")));

		await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
			ResponseEntity<LinkedHashMap> responseEntity = callSakerWithAktoerId();
			List<Sak> saker = parseSaker(responseEntity);
			assertThat(OK, is(responseEntity.getStatusCode()));
			assertThat(saker.size(), is(2));
			if (saker.get(0).getArkivsaksystem() == GSAK) {
				assertGsak(saker.get(0));
				assertPsak(saker.get(1));
			} else {
				assertGsak(saker.get(1));
				assertPsak(saker.get(0));
			}
		});
	}


	@Test
	void shouldGetSakerForAktoerID() {
		abacPermit();
		stubFor(post("/pdl")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/hentPdlDataForIdent-happy.json")));
		stubFor(get("/gsak?aktoerId=" + AKTOER_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("gsak/gsak-sakerBySaksId-happy.json")));
		stubFor(get("/pen/springapi/sak/sammendrag")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy.json")));

		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			ResponseEntity<LinkedHashMap> responseEntity = callSakerWithAktoerId();
			List<Sak> saker = parseSaker(responseEntity);
			assertThat(OK, is(responseEntity.getStatusCode()));
			assertThat(saker.size(), is(2));
			if (saker.get(0).getArkivsaksystem() == GSAK) {
				assertGsak(saker.get(0));
				assertPsak(saker.get(1));
			} else {
				assertGsak(saker.get(1));
				assertPsak(saker.get(0));
			}
		});
	}

	@Test
	void shouldReturnNoSakerWhenDenyOnPep1g() throws Exception {
		abacDenyPep1g();
		stubFor(post("/pdl")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/hentPdlDataForIdent-happy.json")));
		ResponseEntity<LinkedHashMap> responseEntity = callSakerWithAktoerId();
		List<Sak> saker = parseSaker(responseEntity);

		assertThat(saker, hasSize(0));
	}

	@Test
	void shouldReturnNoSakerWhenDenyOnPep2() throws Exception {
		abacDenyPep2();
		stubFor(post("/pdl")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/hentPdlDataForIdent-happy.json")));
		stubFor(get("/gsak?aktoerId=" + AKTOER_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("gsak/gsak-sakerByFagsakIdAndFagsaksystem-FAR-happy.json")));
		stubFor(get("/pen/springapi/sak/sammendrag")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-empty.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callSakerWithAktoerId();
		List<Sak> saker = parseSaker(responseEntity);
		assertThat(saker.size(), is(0));
		verifyabacDenyPep2AndHttpStatusCode(OK, responseEntity.getStatusCode());
	}

	@Test
	void shouldReturnNoSakerWhenDenyOnPep3() throws Exception {
		abacDenyPep3SkipPep2dAndPep2();
		stubFor(post("/pdl")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/hentPdlDataForIdent-happy.json")));
		stubFor(get("/gsak?aktoerId=" + AKTOER_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("gsak/gsak-sakerBySaksId-happy.json")));
		stubFor(get("/pen/springapi/sak/sammendrag")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
						.withBodyFile("psak/psak/psak-hentSakSammendragListe-happy-empty.json")));
		stubFor(get("/bidrag/654321").willReturn(aResponse().withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callSakerWithAktoerId();
		List<Sak> saker = parseSaker(responseEntity);
		assertThat(saker, hasSize(0));
		verifyabacDenyPep3SkipPep2AndPep2dAndHttpStatusCode(OK, responseEntity.getStatusCode());
	}

	private void assertGsak(Sak gsak) {
		assertThat(gsak.getArkivsaksnummer(), is("135695442"));
		assertThat(gsak.getArkivsaksystem(), is(GSAK));
		assertThat(gsak.getDatoOpprettet(), is(LocalDateTime.parse("2018-07-17T11:49:01", ISO_LOCAL_DATE_TIME)));
		assertThat(gsak.getFagsaksystem(), is("BISYS"));
		assertThat(gsak.getFagsakId(), is("654321"));
		assertThat(gsak.getSakstype(), is(FAGSAK));
		assertThat(gsak.getTema(), is(BID));
	}

	private void assertPsak(Sak psak) {
		assertThat(psak.getArkivsaksnummer(), is("21998969"));
		assertThat(psak.getArkivsaksystem(), is(PSAK));
		assertThat(psak.getDatoOpprettet(), is(LocalDateTime.parse("2015-06-01T00:00", ISO_LOCAL_DATE_TIME)));
		assertThat(psak.getFagsaksystem(), is("PP01"));
		assertThat(psak.getFagsakId(), is("21998969"));
		assertThat(psak.getSakstype(), is(FAGSAK));
		assertThat(psak.getTema(), is(UFO));
	}

	private ResponseEntity<LinkedHashMap> callSakerWithAktoerId() throws IOException, URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("saker/saker_aktoerid.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, LinkedHashMap.class);
	}

	private List<Sak> parseSaker(ResponseEntity<LinkedHashMap> responseEntity) {
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		return objectMapper.convertValue(responseEntityData.get("saker"), new TypeReference<>() {
		});
	}
}
