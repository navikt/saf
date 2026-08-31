package no.nav.saf.endpoints;

import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.endpoints.graphql.GraphQLRequest;
import no.nav.saf.endpoints.graphql.GraphQLResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.RequestEntity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Kjører dokumentoversiktBruker og journalpost-spørringene i sekvens for samme journalpost,
 * og verifiserer at journalpostId og dokumentInfoId er like på tvers av begge endepunktene.
 */
class DokumentoversiktOgJournalpostIT extends AbstractItest {

	@BeforeEach
	void setUp() {
		setupHappyPathAzureToken();
		stubTexasToken();
		tilgangskontrollPermit();
	}

	@Test
	void shouldReturnMatchingJournalpostIdOgDokumentInfoIdFraDokumentoversiktBrukerOgJournalpost() throws URISyntaxException {
		stubPdl("hentPdlDataForIdent-inngaaendeBrevBruker-happy.json");
		stubSakMedAktoerId("sak-sakerBySaksId-429837417-happy.json");
		stubFinnjournalposter("finnjournalposter-happy-429837417.json");
		stubPensjonSakSammendrag();
		stubDokarkivJournalpost();

		Dokumentoversikt dokumentoversikt = getDokumentoversikt(callDokumentOversikBrukerWithAktoerId());
		Journalpost journalpostFraDokumentoversikt = dokumentoversikt.getJournalposter().getFirst();
		Journalpost journalpostFraJournalpostSpoerring = parseJournalpost(journalpostQuery());

		assertThat(journalpostFraDokumentoversikt.getJournalpostId()).isEqualTo(journalpostFraJournalpostSpoerring.getJournalpostId());
		assertThat(journalpostFraDokumentoversikt.getDokumenter().getFirst().getDokumentInfoId())
				.isEqualTo(journalpostFraJournalpostSpoerring.getDokumenter().getFirst().getDokumentInfoId());

		assertThat(journalpostFraDokumentoversikt.getDokumenter().getFirst().getDokumentvarianter().getFirst().isSaksbehandlerHarTilgang())
			.isEqualTo(journalpostFraJournalpostSpoerring.getDokumenter().getFirst().getDokumentvarianter().getFirst().isSaksbehandlerHarTilgang());

		assertThat(journalpostFraDokumentoversikt.getDokumenter().getFirst().getDokumentvarianter().getFirst().isSaksbehandlerHarTilgang()).isTrue();
	}

	private ResponseEntity<GraphQLResponse> callDokumentOversikBrukerWithAktoerId() throws URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("dokumentoversiktBruker/dokumentoversiktbruker_aktoerid_inkl_feilregistrert.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, GraphQLResponse.class);
	}

	@SuppressWarnings("unchecked")
	private Dokumentoversikt getDokumentoversikt(ResponseEntity<GraphQLResponse> responseEntity) {
		Map<String, Object> responseEntityData = (Map<String, Object>) responseEntity.getBody().getData().get("dokumentoversiktBruker");
		return jsonMapper.convertValue(responseEntityData, Dokumentoversikt.class);
	}

	private GraphQLResponse journalpostQuery() throws URISyntaxException {
		GraphQLRequest request = new GraphQLRequest(stringFromClasspath("journalpost/journalpost_429837417.query"), null, null);
		RequestEntity<GraphQLRequest> requestEntity = new RequestEntity<>(request, createHeaders(), HttpMethod.POST, new URI("/graphql"));
		return restTemplate.exchange(requestEntity, GraphQLResponse.class).getBody();
	}

	private Journalpost parseJournalpost(GraphQLResponse graphQLResponse) {
		Object journalpost = graphQLResponse.getData().get("journalpost");
		return jsonMapper.convertValue(journalpost, Journalpost.class);
	}

	private static void stubDokarkivJournalpost() {
		stubFor(get("/dokarkiv/journalpost/journalpostId/429837417")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("safintern/journalpost/journalpost-sak-inngaaende-happy-429837417.json")));
	}
}
