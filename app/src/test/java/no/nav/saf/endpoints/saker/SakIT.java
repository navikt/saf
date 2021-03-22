package no.nav.saf.endpoints.saker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.Sakstype;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.visningsmodell.Sak;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class SakIT extends AbstractItest {

	private static final String AKTOER_ID = "1912374211459";
	private static final ObjectMapper objectMapper = new ObjectMapper();

	static {
		objectMapper.registerModule(new JavaTimeModule());
	}

	@Test
	public void shouldGetSakerForAktoerID() throws IOException, URISyntaxException {
		abacPermit();
		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerId-happy.xml")));
		stubFor(get("/gsak?aktoerId=" + AKTOER_ID)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("gsak/gsak-sakerBySaksId-happy.json")));
		stubFor(post("/pensjonsakv1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy.xml")));


		ResponseEntity<LinkedHashMap> responseEntity = callSakerWithAktoerId();
		List<Sak> saker = parseSaker(responseEntity);
		assertThat(HttpStatus.OK, is(responseEntity.getStatusCode()));
		assertThat(saker.size(), is(2));
		if(saker.get(0).getArkivsaksystem() == Arkivsakssystem.GSAK) {
			assertGsak(saker.get(0));
			assertPsak(saker.get(1));
		}
		else {
			assertGsak(saker.get(1));
			assertPsak(saker.get(0));
		}
}

	@Test
	void shouldReturnNoSakerWhenDenyOnPep1g() throws Exception {
		abacDenyPep1g();

		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerId-happy.xml")));

		ResponseEntity<LinkedHashMap> responseEntity = callSakerWithAktoerId();
		List<Sak> saker = parseSaker(responseEntity);

		assertThat(saker, hasSize(0));
	}

	@Test
	void shouldReturnNoSakerWhenDenyOnPep2() throws Exception {
		abacDenyPep2();
		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerId-happy.xml")));
		stubFor(get("/gsak?aktoerId=" + AKTOER_ID)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("gsak/gsak-sakerBySaksId-happy.json")));
		stubFor(post("/pensjonsakv1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-empty.xml")));
		stubFor(get("/bidrag/654321").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callSakerWithAktoerId();
		List<Sak> saker = parseSaker(responseEntity);
		assertThat(saker.size(), is(0));
		verifyabacDenyPep2AndHttpStatusCode(HttpStatus.OK, responseEntity.getStatusCode());

	}

	@Test
	void shouldReturnNoSakerWhenDenyOnPep3() throws Exception {
		abacDenyPep3Withoutpep2d();
		stubFor(post("/aktoerv2")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("aktoerV2/hentIdentForAktoerId-happy.xml")));
		stubFor(get("/gsak?aktoerId=" + AKTOER_ID)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("gsak/gsak-sakerBySaksId-happy.json")));
		stubFor(post("/pensjonsakv1")
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withBodyFile("psak/psak/psak-hentSakSammendragListe-happy-empty.xml")));
		stubFor(get("/bidrag/654321").willReturn(aResponse().withStatus(HttpStatus.OK.value())
				.withHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/bidragsak-happy.json")));

		ResponseEntity<LinkedHashMap> responseEntity = callSakerWithAktoerId();
		List<Sak> saker = parseSaker(responseEntity);
		assertThat(saker, hasSize(0));
		verifyabacDenyPep3NoPep2dAndHttpStatusCode(HttpStatus.OK, responseEntity.getStatusCode());
	}

	private void assertGsak(Sak gsak){
		assertThat(gsak.getArkivsaksnummer(), is("135695442"));
		assertThat(gsak.getArkivsaksystem(), is(Arkivsakssystem.GSAK));
		assertThat(gsak.getDatoOpprettet(), is(LocalDateTime.parse("2018-07-17T11:49:01", DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
		assertThat(gsak.getFagsaksystem(), is("FS22"));
		assertThat(gsak.getFagsakId(), is("654321"));
		assertThat(gsak.getSakstype(), is(Sakstype.GENERELL_SAK));

	}

	private void assertPsak(Sak psak){
		assertThat(psak.getArkivsaksnummer(), is("21998969"));
		assertThat(psak.getArkivsaksystem(), is(Arkivsakssystem.PSAK));
		assertThat(psak.getDatoOpprettet(), is(LocalDateTime.parse("2015-06-01T00:00", DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
		assertThat(psak.getFagsaksystem(), is("PP01"));
		assertThat(psak.getFagsakId(), is("21998969"));
		assertThat(psak.getSakstype(), is(Sakstype.FAGSAK));
	}


	private ResponseEntity<LinkedHashMap> callSakerWithAktoerId() throws IOException, URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("saker/saker_aktoerid.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, LinkedHashMap.class);
	}


	private List<Sak> parseSaker(ResponseEntity<LinkedHashMap> responseEntity) {
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		return objectMapper.convertValue(responseEntityData.get("saker"), new TypeReference<List<Sak>>() {
		});
	}


}
