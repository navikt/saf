package no.nav.saf.endpoints.saker;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import no.nav.saf.domain.visningsmodell.Sak;
import no.nav.saf.endpoints.AbstractItest;
import no.nav.saf.endpoints.graphql.GraphQLRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.Sakstype.FAGSAK;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.Sakstype.GENERELL_SAK;
import static no.nav.saf.domain.kode.Arkivsakssystem.GSAK;
import static no.nav.saf.domain.kode.Arkivsakssystem.PSAK;
import static no.nav.saf.domain.kode.Tema.AAP;
import static no.nav.saf.domain.kode.Tema.BID;
import static no.nav.saf.domain.kode.Tema.OPP;
import static no.nav.saf.domain.kode.Tema.PEN;
import static no.nav.saf.domain.kode.Tema.UFO;
import static org.assertj.core.api.Assertions.tuple;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpStatus.OK;

class SakerIT extends AbstractItest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

	@BeforeEach
	void setup() {
		setupHappyPathAzureToken();
		stubTexasToken();
	}

	@Test
	void shouldReturnOldestSakWhenDuplicates() {
		tilgangskontrollPermit();
		stubPdl();
		stubSakMedAktoerId("sak-sakerBySaksId-happy-duplicates.json");
		stubPensjonSakSammendrag("psak-hentSakSammendragListe-happy.json");

		await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
			ResponseEntity<LinkedHashMap> responseEntity = callSakerWithAktoerId();
			List<Sak> saker = parseSaker(responseEntity);
			assertThat(OK, is(responseEntity.getStatusCode()));
			assertThat(saker.size(), is(2));
			if (saker.get(0).getArkivsaksystem() == GSAK) {
				assertSak(saker.get(0));
				assertPsak(saker.get(1));
			} else {
				assertSak(saker.get(1));
				assertPsak(saker.get(0));
			}
		});
	}

	@Test
	void shouldReturnPsakWhenDuplicatesContainsPsak() {
		tilgangskontrollPermit();
		stubPdl();
		stubSakMedAktoerId("sak-sakerBySaksId-happy-duplicates-psak.json");
		stubPensjonSakSammendrag("psak-hentSakSammendragListe-happy.json");

		var responseEntity = callSakerWithAktoerId();
		var saker = parseSaker(responseEntity);

		assertThat(OK, is(responseEntity.getStatusCode()));
		assertThat(saker, hasSize(1));
		assertPsak(saker.getFirst());

	}

	@Test
	void shouldReturnPsakAndGenerellSakWhenDuplicatesContainsPsak() {
		tilgangskontrollPermit();
		stubPdl();
		stubSakMedAktoerId("sak-sakerBySaksId-happy-duplicates-psak-with-generell-sak.json");
		stubPensjonSakSammendrag("psak-hentSakSammendragListe-happy.json");

		var responseEntity = callSakerWithAktoerId();
		var saker = parseSaker(responseEntity);

		assertThat(OK, is(responseEntity.getStatusCode()));

		Assertions.assertThat(saker)
				.hasSize(2)
				.extracting("arkivsaksnummer", "arkivsaksystem", "sakstype")
				.containsExactly(
						tuple("135695444", GSAK, GENERELL_SAK),
						tuple("21998969", PSAK, FAGSAK)
				);
	}

	@Test
	void shouldReturnFilteredByTemaWhenDuplicates() {
		tilgangskontrollPermit();
		stubPdl();
		stubSakMedAktoerId("sak-sakerBySaksId-happy-multiple-duplicates.json");
		stubPensjonSakSammendrag("psak-hentSakSammendragListe-happy.json");

		var responseEntity = callSakerWithAktoerId();
		var saker = parseSaker(responseEntity);

		assertThat(OK, is(responseEntity.getStatusCode()));

		Assertions.assertThat(saker)
				.hasSize(5)
				.extracting("arkivsaksnummer", "arkivsaksystem", "tema")
				.containsExactly(
						tuple("135695445", GSAK, PEN),
						tuple("135695447", GSAK, AAP),
						tuple("135695448", GSAK, OPP),
						tuple("135695449", GSAK, OPP),
						tuple("21998969", PSAK, UFO)
				);
	}

	@Test
	void shouldGetSakerForAktoerID() {
		tilgangskontrollPermit();
		stubPdl();
		stubSakMedAktoerId("sak-sakerBySaksId-happy.json");
		stubPensjonSakSammendrag("psak-hentSakSammendragListe-happy.json");

		await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			ResponseEntity<LinkedHashMap> responseEntity = callSakerWithAktoerId();
			List<Sak> saker = parseSaker(responseEntity);
			assertThat(OK, is(responseEntity.getStatusCode()));
			assertThat(saker.size(), is(2));
			if (saker.get(0).getArkivsaksystem() == GSAK) {
				assertSak(saker.get(0));
				assertPsak(saker.get(1));
			} else {
				assertSak(saker.get(1));
				assertPsak(saker.get(0));
			}
		});
	}

	@Test
	void shouldReturnNoSakerWhenDenyOnPep1g() {
		tilgangskontrollDenyPep1g();
		stubPdl();
		ResponseEntity<LinkedHashMap> responseEntity = callSakerWithAktoerId();
		List<Sak> saker = parseSaker(responseEntity);

		assertThat(saker, hasSize(0));
	}

	@Test
	void shouldReturnNoSakerWhenDenyOnPep2() {
		tilgangskontrollDenyPep2();
		stubPdl();
		stubSakMedAktoerId("sak-sakerByFagsakIdAndFagsaksystem-FAR-happy.json");
		stubPensjonSakSammendrag("psak-hentSakSammendragListe-happy-empty.json");

		ResponseEntity<LinkedHashMap> responseEntity = callSakerWithAktoerId();
		List<Sak> saker = parseSaker(responseEntity);
		assertThat(saker.size(), is(0));
		verifyDenyPep2(responseEntity.getStatusCode());
	}

	@Test
	void shouldReturnNoSakerWhenDenyOnPep3() {
		tilgangskontrollDenyPep3();
		stubPdl();
		stubSakMedAktoerId("sak-sakerBySaksId-happy.json");
		stubPensjonSakSammendrag("psak-hentSakSammendragListe-happy-empty.json");
		stubBidrag("bidragsak-happy.json");

		ResponseEntity<LinkedHashMap> responseEntity = callSakerWithAktoerId();
		List<Sak> saker = parseSaker(responseEntity);
		assertThat(saker, hasSize(0));
		verifyTilgangsmaskinenDenyPep3AndHttpStatusCode(responseEntity.getStatusCode());
	}

	private void assertSak(Sak sak) {
		assertThat(sak.getArkivsaksnummer(), is("135695442"));
		assertThat(sak.getArkivsaksystem(), is(GSAK));
		assertThat(sak.getDatoOpprettet(), is(LocalDateTime.parse("2018-07-17T13:49:01", ISO_LOCAL_DATE_TIME)));
		assertThat(sak.getFagsaksystem(), is("BISYS"));
		assertThat(sak.getFagsakId(), is("654321"));
		assertThat(sak.getSakstype(), is(FAGSAK));
		assertThat(sak.getTema(), is(BID));
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

	@SneakyThrows
	private ResponseEntity<LinkedHashMap> callSakerWithAktoerId() {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("saker/saker_aktoerid.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, LinkedHashMap.class);
	}

	private List<Sak> parseSaker(ResponseEntity<LinkedHashMap> responseEntity) {
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		return OBJECT_MAPPER.convertValue(responseEntityData.get("saker"), new TypeReference<>() {
		});
	}
}
