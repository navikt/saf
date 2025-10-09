package no.nav.saf.endpoints;

import lombok.SneakyThrows;
import no.nav.saf.ApplicationConfig;
import no.nav.saf.anticorruptionlayer.nav.NavOrgService;
import no.nav.saf.azure.AzureProperties;
import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.endpoints.testconfig.ValkeyCacheTestConfig;
import no.nav.saf.headers.NavHeaders;
import no.nav.saf.integration.azure.AzureTokenConsumer;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.Options.DYNAMIC_PORT;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostMapper.SKJULT_TITTEL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(classes = {AbstractItest.TestConfig.class, ValkeyCacheTestConfig.class, ApplicationConfig.class},
		webEnvironment = RANDOM_PORT,
		properties = {"spring.main.allow-bean-definition-overriding=true"})
@ActiveProfiles(value = {"itest", "wiremock"})
@EnableMockOAuth2Server
@AutoConfigureWireMock(port = DYNAMIC_PORT)
public abstract class AbstractItest {

	private static final String SCENARIO_ABAC = "state_abac";
	private static final String STATE_PERMIT = "state_permit";
	private static final String STATE_PEP2 = "state_pep2";
	private static final String STATE_PEP2D = "state_pep2d";
	private static final String STATE_PEP3 = "state_pep3";
	private static final String STATE_PEP7D = "state_pep7d";

	protected static final String NAV_IDENT_SAKSBEHANDLER = "Z123456";
	protected static final String MS_ID_SAKSBEHANDLER = "11111111-2222-3333-4444-555555555555";
	protected static final String ORG_NR = "894705922";
	protected static final String AKTOER_ID = "1912374211459";
	protected static final String PENSJON_API_SAK_SAMMENDRAG_URL = "/pensjon/api/sak/sammendrag";
	protected static final String PENSJON_API_PIP_HENT_BRUKER_OG_ENHETSTILGANGER_FOR_SAK_V1_URL = "/pensjon/api/pip/hentBrukerOgEnhetstilgangerForSak/v1";
	protected static final String SAK_API_PATH_MED_QUERY_PARA_AKTOER_ID = "/sak?aktoerId=" + AKTOER_ID;
	protected static final String SAK_API_PATH_MED_QUERY_PARA_FAGSAK_NR = "/sak?fagsakNr=ARENA-1&applikasjon=AO01";


	@Configuration
	public static class TestConfig {

		@Bean
		@Primary
		ClientHttpRequestFactory clientHttpRequestFactoryTest() {
			return new SimpleClientHttpRequestFactory();
		}

		@Bean
		@Primary
		AzureTokenConsumer azureTokenConsumer(AzureProperties azureProperties, RestTemplateBuilder restTemplateBuilder) {
			var httpClient = HttpClients.custom()
					.build();
			var clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
			clientHttpRequestFactory.setConnectTimeout(5_000);

			return new AzureTokenConsumer(azureProperties, restTemplateBuilder, clientHttpRequestFactory);
		}
	}

	@Autowired
	protected TestRestTemplate restTemplate;
	@Autowired
	private MockOAuth2Server server;
	@MockitoBean
	protected NavOrgService navOrgService;
	@Autowired
	protected StringRedisTemplate stringRedisTemplate;

	@AfterEach
	void resetCache() {
		stringRedisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
	}

	protected HttpEntity<?> createHttpEntity() {
		return new HttpEntity<>(createHeaders());
	}

	protected HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
		headers.setBearerAuth(getOnBehalfOfToken());
		return headers;
	}

	protected HttpHeaders createHeadersNavUserId() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
		headers.setBearerAuth(getClientCredentialToken());
		headers.set(NavHeaders.NAV_USER_ID, NAV_IDENT_SAKSBEHANDLER);
		return headers;
	}

	protected HttpHeaders createHeadersClientCredential() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
		headers.setBearerAuth(getClientCredentialToken());
		return headers;
	}

	protected HttpHeaders createHeadersClientCredentialWithoutRoles() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
		headers.setBearerAuth(getClientCredentialTokenWithoutRoles());
		return headers;
	}

	private String getOnBehalfOfToken() {
		return jwt(NAV_IDENT_SAKSBEHANDLER,
				Map.of(
						"oid", UUID.randomUUID().toString(),
						"sub", UUID.randomUUID().toString(),
						"azp_name", "dev-itest:isa:gosys",
						"NAVident", NAV_IDENT_SAKSBEHANDLER
				)
		);
	}

	private String getClientCredentialToken() {
		String oidSubEqual = UUID.randomUUID().toString();
		return jwt("dev-itest:isa:gosys",
				Map.of(
						"oid", oidSubEqual,
						"sub", oidSubEqual,
						"azp_name", "dev-itest:isa:gosys",
						"roles", List.of("tema_hje")
				)
		);
	}

	private String getClientCredentialTokenWithoutRoles() {
		String oidSubEqual = UUID.randomUUID().toString();
		return jwt("dev-itest:isa:gosys",
				Map.of(
						"oid", oidSubEqual,
						"sub", oidSubEqual,
						"azp_name", "dev-itest:isa:gosys"
				)
		);
	}

	protected String jwt(String subject, Map<String, Object> claims) {
		String issuerId = "azurev2";
		String audience = "dev-itest:teamdokumenthandtering:saf";
		return server.issueToken(
				issuerId,
				"gosys-clientid",
				new DefaultOAuth2TokenCallback(
						issuerId,
						subject,
						"JWT",
						List.of(audience),
						claims,
						60
				)
		).serialize();
	}

	protected static void setupHappyPathAzureToken() {
		stubFor(post("/azure_token")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response_dummy.json")));
	}

	protected void stubNavHrOrganisasjonJa(String organisasjonsnummer) {
		when(navOrgService.isOrganisasjonsnummerNavBedrift(organisasjonsnummer)).thenReturn(true);
	}

	protected void stubNavHrOrganisasjonNei(String organisasjonsnummer) {
		when(navOrgService.isOrganisasjonsnummerNavBedrift(organisasjonsnummer)).thenReturn(false);
	}

	protected void stubMsGraphMemberOfEgenAnsattDefaultSaksbehandler() {
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfSeveralGroups(MS_ID_SAKSBEHANDLER, "nav/msgraph-checkmembergroup-egenansatt.json");
	}

	protected static void stubPdl() {
		stubPdl("hentPdlDataForIdent-happy.json");
	}

	protected static void stubPdl(String filename) {
		stubFor(post("/pdl")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("pdl/" + filename)));
	}

	protected static void stubSakMedAktoerId() {
		stubSakMedAktoerId("sak-sakerBySaksId_not_bid-happy.json");
	}

	protected static void stubSakMedAktoerId(String filename) {
		stubFor(get(SAK_API_PATH_MED_QUERY_PARA_AKTOER_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("sak/" + filename)));
	}

	protected void stubSakMedFagSak(String filenavn) {
		stubFor(get(SAK_API_PATH_MED_QUERY_PARA_FAGSAK_NR)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile(filenavn)));
	}

	protected static void stubSakOrgnr() {
		stubFor(get("/sak?orgnr=" + ORG_NR)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("sak/sak-sakerBySaksId_not_bid-happy.json")));
	}

	protected static void stubFinnjournalposter() {
		stubFinnjournalposter("finnjournalposter-happy.json");
	}

	protected static void stubFinnjournalposter(String filename) {
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("joark/" + filename)));
	}

	protected static void stubPensjonSakSammendrag() {
		stubPensjonSakSammendrag("psak-hentSakSammendragListe-happy.json");
	}

	protected static void stubPensjonSakSammendrag(String filename) {
		stubFor(get(PENSJON_API_SAK_SAMMENDRAG_URL)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("psak/" + filename)));
	}

	protected static void stubPensjonBrukerForSak() {
		stubFor(get(PENSJON_API_PIP_HENT_BRUKER_OG_ENHETSTILGANGER_FOR_SAK_V1_URL)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("psak/psak-hentBrukerForSak-happy.json")));
	}

	protected static void stubBidrag() {
		stubBidrag("bidragsak-happy.json");
	}

	protected static void stubBidrag(String filename) {
		stubFor(get("/bidrag/654321").willReturn(aResponse().withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/" + filename)));
	}

	private static void stubTexasExchangeOboToken() {
		stubFor(post("/texas").willReturn(aResponse().withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("texas/texas_happy.json")));
	}

	public static void stubTexasToken() {
		stubFor(post("/nais/token").willReturn(aResponse().withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("texas/texas_happy.json")));
	}

	protected static void stubTilgangsmaskinenPermit() {
		stubTexasExchangeOboToken();
		stubFor(post("/tilgangsmaskinen/api/v1/komplett").willReturn(aResponse().withStatus(NO_CONTENT.value())));
	}

	protected static void stubTilgangsmaskinenDeny() {
		stubTexasExchangeOboToken();
		stubFor(post("/tilgangsmaskinen/api/v1/komplett").willReturn(aResponse().withStatus(FORBIDDEN.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("tilgangsmaskinen/" + "tilgangsmaskinen_deny_fortrolig.json")));
	}

	protected static void abacPermit() {
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
		stubMsGraphMemberOfAllRelevantGroupsDefaultSaksbehandler();
		stubTilgangsmaskinenPermit();
	}

	protected void abacDenyPep6dSkipPep3OrPep2() {
		stubTilgangsmaskinenPermit();
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfSeveralGroups(MS_ID_SAKSBEHANDLER, "nav/msgraph-checkmembergroup-ikke-joarkvedlikehold.json");
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected void abacDenyPep6dSkipPep2Pep4Pep5() {
		stubTilgangsmaskinenPermit();
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfSeveralGroups(MS_ID_SAKSBEHANDLER, "nav/msgraph-checkmembergroup-egenansatt-fortrolig-strengt-fortrolig.json");
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP3));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP3)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected void abacDenyPep6dSkipPep2Pep3Pep4Pep5() {
		stubTilgangsmaskinenPermit();
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfSeveralGroups(MS_ID_SAKSBEHANDLER, "nav/msgraph-checkmembergroup-egenansatt-fortrolig-strengt-fortrolig.json");
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PERMIT)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected void abacDenyPep5SkipPep2OrPep3() {
		stubTilgangsmaskinenPermit();
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfSeveralGroups(MS_ID_SAKSBEHANDLER, "nav/msgraph-checkmembergroup-ikke-joarkvedlikehold.json");
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected void abacDenyPep5SkipPep4() {
		stubTilgangsmaskinenPermit();
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfSeveralGroups(MS_ID_SAKSBEHANDLER, "nav/msgraph-checkmembergroup-ikke-joarkvedlikehold.json");
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected void abacDenyPep5SkipPep2Pep3Pep4() {
		stubTilgangsmaskinenPermit();
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfSeveralGroups(MS_ID_SAKSBEHANDLER, "nav/msgraph-checkmembergroup-ikke-joarkvedlikehold.json");
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PERMIT));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PERMIT)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected void abacDenyPep5SkipPep1gPep2Pep2dPep3() {
		stubTilgangsmaskinenPermit();
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfSeveralGroups(MS_ID_SAKSBEHANDLER, "nav/msgraph-checkmembergroup-ikke-joarkvedlikehold.json");
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected void abacDenyPep4SkipPep1gPep2Pep2dPep3() {
		stubTilgangsmaskinenPermit();
		stubMsGraphMemberOfNoGroupsDefaultSaksbehandler();
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json"))
				.willSetStateTo(STATE_PERMIT));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PERMIT)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected void abacDenyPep4SkipPep2Pep3() {
		stubTilgangsmaskinenPermit();
		stubMsGraphMemberOfNoGroupsDefaultSaksbehandler();
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected void abacDenyPep4SkipPep2OrPep3() {
		stubTilgangsmaskinenPermit();
		stubMsGraphMemberOfNoGroupsDefaultSaksbehandler();
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PERMIT));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PERMIT)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected void abacDenyPep3SkipPep2() {
		stubTilgangsmaskinenPermit();
		stubMsGraphMemberOfNoGroupsDefaultSaksbehandler();
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP3));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP3)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny-kode6.json"))
				.willSetStateTo(STATE_PERMIT));
	}

	protected void abacDenyPep3SkipPep2dAndPep2() {
		stubTilgangsmaskinenPermit();
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP3));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP3)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json"))
				.willSetStateTo(STATE_PERMIT));
	}

	protected void abacDenyPep2d() {
		stubTilgangsmaskinenPermit();
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json"))
				.willSetStateTo(STATE_PERMIT));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PERMIT)
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected void abacDenyPep2dSkipPep2() {
		stubTilgangsmaskinenPermit();
		stubMsGraphMemberOfAllRelevantGroupsDefaultSaksbehandler();
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json"))
				.willSetStateTo(STATE_PERMIT));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PERMIT)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected void abacDenyPep2() {
		stubTilgangsmaskinenPermit();
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));
	}

	protected void abacDenyPep2MidlertidigJournalpost() {
		stubTilgangsmaskinenPermit();
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));
	}

	protected void abacDenyPep1g() {
		stubTilgangsmaskinenDeny();
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json"))
				.willSetStateTo(STATE_PERMIT));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PERMIT)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected void abacDenyPep7dSkipPep2Pep3Pep4Pep5Pep6d() {
		stubTilgangsmaskinenPermit();
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP2D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP2D)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP7D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP7D)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));
	}

	protected static void denyPep8d() {
		stubTilgangsmaskinenPermit();
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfSeveralGroups(MS_ID_SAKSBEHANDLER, "nav/msgraph-checkmembergroup-ikke-joark-historisk.json");
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected static void stubMsGraphGetUser(String navIdent) {
		stubFor(get("/msgraph/users?$count=true&$filter=onPremisesSamAccountName%20eq%20%27" + navIdent + "%27&$select=id")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("nav/msgraph-users.json")));
	}

	protected static void stubMsGraphMemberOfSeveralGroups(String msUserId, String bodyFile) {
		stubFor(post(urlMatching("/msgraph/users/" + msUserId + "/checkMemberGroups"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile(bodyFile)));
	}

	protected static void stubMsGraphMemberOfAllRelevantGroupsDefaultSaksbehandler() {
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfSeveralGroups(MS_ID_SAKSBEHANDLER, "nav/msgraph-checkmembergroup-alle-relevante-grupper.json");
	}

	protected static void stubMsGraphMemberOfNoGroupsDefaultSaksbehandler() {
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfSeveralGroups(MS_ID_SAKSBEHANDLER, "nav/msgraph-checkmembergroup-ingen-grupper.json");
	}

	protected void verifyMsGraphMemberOfSeveralGroupsCalled(String msUserId, int count) {
		verify(count, postRequestedFor(urlMatching("/msgraph/users/" + msUserId + "/checkMemberGroups")));
	}

	protected void verifyabacDenyPep7dSkipPep2Pep3Pep4Pep5Pep6dAndHttpStatusCode(HttpStatusCode expectedHttpStatus, HttpStatusCode actualHttpStatus) {
		verify(3, postRequestedFor(urlEqualTo("/abac")));
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep1gAndHttpStatusCode(HttpStatusCode expectedHttpStatus, HttpStatusCode actualHttpStatus) {
		verify(1, postRequestedFor(urlEqualTo("/abac")));
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep2AndHttpStatusCode(HttpStatusCode expectedHttpStatus, HttpStatusCode actualHttpStatus) {
		verify(2, postRequestedFor(urlEqualTo("/abac")));
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep2dAndHttpStatusCode(boolean isDokumentoversikt, HttpStatusCode expectedHttpStatus, HttpStatusCode actualHttpStatus) {
		if (isDokumentoversikt) {
			verify(3, postRequestedFor(urlEqualTo("/abac")));
		} else {
			verify(3, postRequestedFor(urlEqualTo("/abac")));
		}
		verifyMsGraphMemberOfSeveralGroupsCalled(MS_ID_SAKSBEHANDLER, 1);
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep3SkipPep2AndPep2dAndHttpStatusCode(HttpStatusCode expectedHttpStatus, HttpStatusCode actualHttpStatus) {
		verify(2, postRequestedFor(urlEqualTo("/abac")));
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep4SkipPep2OrPep3AndHttpStatusCode(HttpStatusCode expectedHttpStatus, HttpStatusCode actualHttpStatus) {
		verify(3, postRequestedFor(urlEqualTo("/abac")));
		verifyMsGraphMemberOfSeveralGroupsCalled(MS_ID_SAKSBEHANDLER, 1);
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep5SkipPep2OrPep3AndHttpStatusCode(boolean isDokumentoversikt, HttpStatusCode expectedHttpStatus, HttpStatusCode actualHttpStatus) {
		if (isDokumentoversikt) {
			verify(3, postRequestedFor(urlEqualTo("/abac")));
		} else {
			verify(3, postRequestedFor(urlEqualTo("/abac")));
		}
		verifyMsGraphMemberOfSeveralGroupsCalled(MS_ID_SAKSBEHANDLER, 1);
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep6dSkipPep2AndHttpStatusCode(HttpStatusCode expectedHttpStatus, HttpStatusCode actualHttpStatus) {
		verify(3, postRequestedFor(urlEqualTo("/abac")));
		verifyMsGraphMemberOfSeveralGroupsCalled(MS_ID_SAKSBEHANDLER, 1);
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep6dSkipPep2Pep3AndHttpStatusCode(HttpStatusCode expectedHttpStatus, HttpStatusCode actualHttpStatus) {
		verify(2, postRequestedFor(urlEqualTo("/abac")));
		verifyMsGraphMemberOfSeveralGroupsCalled(MS_ID_SAKSBEHANDLER, 1);
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyEmptyJournalpostListeAndNullSideInfo(Dokumentoversikt dokumentoversikt) {
		assertEquals(0, dokumentoversikt.getJournalposter().size());
		assertNull(dokumentoversikt.getSideInfo());
	}

	protected void verifyEmptyJournalpostListeAndEmptySideInfo(Dokumentoversikt dokumentoversikt) {
		assertEquals(0, dokumentoversikt.getJournalposter().size());
		assertNull(dokumentoversikt.getSideInfo().getSluttpeker());
		assertFalse(dokumentoversikt.getSideInfo().isFinnesNesteSide());
	}

	protected void assertSaksbehandlerHarTilgang(Dokumentoversikt dokumentoversikt) {
		dokumentoversikt.getJournalposter().forEach(
				journalpost -> journalpost.getDokumenter().forEach(
						dokumentInfo -> dokumentInfo.getDokumentvarianter().forEach(
								dokumentvariant -> assertTrue(dokumentvariant.isSaksbehandlerHarTilgang())))
		);
	}

	protected void assertSaksbehandlerHarIkkeTilgang(Dokumentoversikt dokumentoversikt) {
		dokumentoversikt.getJournalposter().forEach(
				journalpost -> journalpost.getDokumenter().forEach(
						dokumentInfo -> dokumentInfo.getDokumentvarianter().forEach(
								dokumentvariant -> assertFalse(dokumentvariant.isSaksbehandlerHarTilgang())))
		);
	}

	protected void assertSkjultTittel(Dokumentoversikt dokumentoversikt) {
		assertThat(dokumentoversikt.getJournalposter()).hasSizeGreaterThan(0);
		dokumentoversikt.getJournalposter().forEach(journalpost -> {
			assertThat(journalpost.getTittel()).isEqualTo(SKJULT_TITTEL);
			assertThat(journalpost.getDokumenter()).hasSizeGreaterThan(0);
			journalpost.getDokumenter().forEach(dokumentInfo -> {
				assertThat(dokumentInfo.getTittel()).isEqualTo(SKJULT_TITTEL);
				assertThat(dokumentInfo.getLogiskeVedlegg()).hasSizeGreaterThan(0);
				dokumentInfo.getLogiskeVedlegg().forEach(logiskVedlegg -> assertThat(logiskVedlegg.getTittel()).isEqualTo(SKJULT_TITTEL));
			});
		});
	}

	protected static void assertBrukerTilgang(Journalpost... journalposter) {
		for (Journalpost journalpost : journalposter) {
			assertTrue(journalpost.isBrukerHarTilgang());
			assertThat(journalpost.getBrukerTilgangAvvistBegrunnelser()).isEmpty();
		}
	}

	@SneakyThrows
	protected String stringFromClasspath(String resourcename) {
		return new String(requireNonNull(this.getClass().getClassLoader().getResourceAsStream(resourcename)).readAllBytes(), UTF_8);
	}
}
