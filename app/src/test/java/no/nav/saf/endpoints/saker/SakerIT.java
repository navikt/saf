package no.nav.saf.endpoints.saker;

import lombok.SneakyThrows;
import no.nav.saf.domain.visningsmodell.Sak;
import no.nav.saf.endpoints.AbstractItest;
import no.nav.saf.endpoints.graphql.GraphQLRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import tools.jackson.core.type.TypeReference;

import java.net.URI;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

class SakerIT extends AbstractItest {

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
		stubBidrag();

		ResponseEntity<LinkedHashMap> responseEntity = callSakerWithAktoerId();
		List<Sak> saker = parseSaker(responseEntity);
		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(saker).hasSize(2)
				.extracting("arkivsaksnummer", "arkivsaksystem", "datoOpprettet", "fagsaksystem", "fagsakId", "sakstype", "tema")
				.containsExactly(
						tuple("135695442", GSAK, LocalDateTime.parse("2018-07-17T13:49:01", ISO_LOCAL_DATE_TIME), "BISYS", "654321", FAGSAK, BID),
						tuple("21998969", PSAK, LocalDateTime.parse("2015-06-01T00:00", ISO_LOCAL_DATE_TIME), "PP01", "21998969", FAGSAK, UFO)
				);
	}

	@Test
	void shouldReturnPsakWhenDuplicatesContainsPsak() {
		tilgangskontrollPermit();
		stubPdl();
		stubSakMedAktoerId("sak-sakerBySaksId-happy-duplicates-psak.json");
		stubPensjonSakSammendrag("psak-hentSakSammendragListe-happy.json");

		var responseEntity = callSakerWithAktoerId();
		var saker = parseSaker(responseEntity);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(saker).hasSize(1)
				.extracting("arkivsaksnummer", "arkivsaksystem", "datoOpprettet", "fagsaksystem", "fagsakId", "sakstype", "tema")
				.containsExactly(
						tuple("21998969", PSAK, LocalDateTime.parse("2015-06-01T00:00", ISO_LOCAL_DATE_TIME), "PP01", "21998969", FAGSAK, UFO)
				);

	}

	@Test
	void shouldReturnPsakAndGenerellSakWhenDuplicatesContainsPsak() {
		tilgangskontrollPermit();
		stubPdl();
		stubSakMedAktoerId("sak-sakerBySaksId-happy-duplicates-psak-with-generell-sak.json");
		stubPensjonSakSammendrag("psak-hentSakSammendragListe-happy.json");

		var responseEntity = callSakerWithAktoerId();
		var saker = parseSaker(responseEntity);

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		assertThat(saker)
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

		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);

		assertThat(saker)
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
		stubBidrag();

		ResponseEntity<LinkedHashMap> responseEntity = callSakerWithAktoerId();
		List<Sak> saker = parseSaker(responseEntity);
		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(saker).hasSize(2)
				.extracting("arkivsaksnummer", "arkivsaksystem", "datoOpprettet", "fagsaksystem", "fagsakId", "sakstype", "tema")
				.containsExactly(
						tuple("135695442", GSAK, LocalDateTime.parse("2018-07-17T13:49:01", ISO_LOCAL_DATE_TIME), "BISYS", "654321", FAGSAK, BID),
						tuple("21998969", PSAK, LocalDateTime.parse("2015-06-01T00:00", ISO_LOCAL_DATE_TIME), "PP01", "21998969", FAGSAK, UFO)
				);
	}

	@Test
	void shouldReturnNoSakerWhenDenyOnPep1g() {
		tilgangskontrollDenyPep1g();
		stubPdl();
		ResponseEntity<LinkedHashMap> responseEntity = callSakerWithAktoerId();
		List<Sak> saker = parseSaker(responseEntity);

		assertThat(saker).isEmpty();
	}

	@Test
	void shouldReturnNoSakerWhenDenyOnPep2() {
		tilgangskontrollDenyPep2();
		stubPdl();
		stubSakMedAktoerId("sak-sakerByFagsakIdAndFagsaksystem-FAR-happy.json");
		stubPensjonSakSammendrag("psak-hentSakSammendragListe-happy-empty.json");
		stubBidragForeldreskap();

		ResponseEntity<LinkedHashMap> responseEntity = callSakerWithAktoerId();
		List<Sak> saker = parseSaker(responseEntity);
		assertThat(saker).isEmpty();
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
		assertThat(saker).isEmpty();
		verifyTilgangsmaskinenDenyPep3AndHttpStatusCode(responseEntity.getStatusCode());
	}

	@Test
	void shouldGetSakerForAktoerIDWhenNavUserIdHeader() {
		tilgangskontrollPermit();
		stubPdl();
		stubSakMedAktoerId("sak-sakerBySaksId-happy.json");
		stubPensjonSakSammendrag("psak-hentSakSammendragListe-happy.json");
		stubBidrag();

		ResponseEntity<LinkedHashMap> responseEntity = callSakerWithAktoerId(createHeadersNavUserId());
		List<Sak> saker = parseSaker(responseEntity);
		assertThat(responseEntity.getStatusCode()).isEqualTo(OK);
		assertThat(saker).hasSize(2)
				.extracting("arkivsaksnummer", "arkivsaksystem", "datoOpprettet", "fagsaksystem", "fagsakId", "sakstype", "tema")
				.containsExactly(
						tuple("135695442", GSAK, LocalDateTime.parse("2018-07-17T13:49:01", ISO_LOCAL_DATE_TIME), "BISYS", "654321", FAGSAK, BID),
						tuple("21998969", PSAK, LocalDateTime.parse("2015-06-01T00:00", ISO_LOCAL_DATE_TIME), "PP01", "21998969", FAGSAK, UFO)
				);
	}

	@Test
	void shouldReturnForbiddenForSakerWhenSystemAndNavUserIdHeaderWithoutRole() {
		var response = callSakerWithAktoerId(createHeadersNavUserIdWithoutRoles());

		assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
		assertThat(response.getBody().get("message")).isEqualTo("Tilgang er avvist. Tjeneste kalt med Nav-User-Id header og maskin-til-maskin Entra token krever role=tilgang_nav_user_id_header.");
	}

	@Test
	void shouldReturnNoSakerWhenDenyOnPep1gWhenNavUserIdHeader() {
		tilgangskontrollDenyPep1g();
		stubPdl();
		ResponseEntity<LinkedHashMap> responseEntity = callSakerWithAktoerId(createHeadersNavUserId());
		List<Sak> saker = parseSaker(responseEntity);

		assertThat(saker).isEmpty();
	}

	@Test
	void shouldReturnNoSakerWhenDenyOnPep2WhenNavUserIdHeader() {
		tilgangskontrollDenyPep2();
		stubPdl();
		stubSakMedAktoerId("sak-sakerByFagsakIdAndFagsaksystem-FAR-happy.json");
		stubPensjonSakSammendrag("psak-hentSakSammendragListe-happy-empty.json");
		stubBidragForeldreskap();

		ResponseEntity<LinkedHashMap> responseEntity = callSakerWithAktoerId(createHeadersNavUserId());
		List<Sak> saker = parseSaker(responseEntity);
		assertThat(saker).isEmpty();
		verifyDenyPep2(responseEntity.getStatusCode());
	}

	@Test
	void shouldReturnNoSakerWhenDenyOnPep3WhenNavUserIdHeader() {
		tilgangskontrollDenyPep3();
		stubPdl();
		stubSakMedAktoerId("sak-sakerBySaksId-happy.json");
		stubPensjonSakSammendrag("psak-hentSakSammendragListe-happy-empty.json");
		stubBidrag("bidragsak-happy.json");

		ResponseEntity<LinkedHashMap> responseEntity = callSakerWithAktoerId(createHeadersNavUserId());
		List<Sak> saker = parseSaker(responseEntity);
		assertThat(saker).isEmpty();
		verifyTilgangsmaskinenDenyPep3AndHttpStatusCode(responseEntity.getStatusCode());
	}

	@SneakyThrows
	private ResponseEntity<LinkedHashMap> callSakerWithAktoerId() {
		return callSakerWithAktoerId(createHeaders());
	}

	@SneakyThrows
	private ResponseEntity<LinkedHashMap> callSakerWithAktoerId(HttpHeaders headers) {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("saker/saker_aktoerid.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, headers, HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, LinkedHashMap.class);
	}

	private List<Sak> parseSaker(ResponseEntity<LinkedHashMap> responseEntity) {
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().get("data");
		return jsonMapper.convertValue(responseEntityData.get("saker"), new TypeReference<>() {
		});
	}
}
