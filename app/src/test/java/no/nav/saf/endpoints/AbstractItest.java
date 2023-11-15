package no.nav.saf.endpoints;

import lombok.SneakyThrows;
import no.nav.saf.ApplicationConfig;
import no.nav.saf.azure.AzureProperties;
import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.headers.NavHeaders;
import no.nav.saf.integration.azure.AzureTokenConsumer;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.Options.DYNAMIC_PORT;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(classes = {AbstractItest.TestConfig.class, ApplicationConfig.class},
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
	private static final String STATE_PEP4 = "state_pep4";
	private static final String STATE_PEP5 = "state_pep5";
	private static final String STATE_PEP6D = "state_pep6d";
	private static final String STATE_PEP7D = "state_pep7d";

	protected static final String NAV_IDENT_SAKSBEHANDLER = "Z123456";
	protected static final String MS_ID_SAKSBEHANDLER = "11111111-2222-3333-4444-555555555555";
	protected static final String ORG_NR = "894705922";
	protected static final String AKTOER_ID = "1912374211459";
	protected static final String PENSJON_SPRINGAPI_SAK_SAMMENDRAG_URL = "/pensjon/springapi/sak/sammendrag";
	protected static final String PENSJON_API_PIP_HENT_BRUKER_OG_ENHETSTILGANGER_FOR_SAK_V1_URL = "/pensjon/api/pip/hentBrukerOgEnhetstilgangerForSak/v1";

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
						"roles", List.of()
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

	protected static void setupHappyPathRestSTS() {
		stubFor(post("/reststs")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("sts/sts-token.json")));
	}

	protected static void setupHappyPathAzureToken() {
		stubFor(post("/azure_token")
				.willReturn(aResponse()
						.withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("azure/token_response_dummy.json")));
	}

	protected static void stubNavHrOrganisasjonJa(String organisasjonsnummer) {
		stubNavHrOrganisasjon(organisasjonsnummer, "hr-nav-organisasjon-ja.json");	}

	protected static void stubNavHrOrganisasjonNei(String organisasjonsnummer) {
		stubNavHrOrganisasjon(organisasjonsnummer, "hr-nav-organisasjon-nei.json");
	}

	protected static void stubNavHrOrganisasjon(String organisasjonsnummer, String filename) {
		stubFor(get("/hrnavorganisasjon/json/Hr/Nav_Orgnummer/ER_NAV_ORGNUMMER?ORGNUMMER_INN=" + organisasjonsnummer)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("nav/" + filename)));
	}

	protected static void stubMsGraphGetUser(String navIdent) {
		stubFor(get("/msgraph/users?%24filter=onPremisesSamAccountName%20eq%20%27" + navIdent + "%27&%24count=true&%24select=id")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("nav/msgraph-users.json")));
	}

	protected static void stubMsGraphMemberOfEgenAnsatt(String msUserId) {
		stubFor(get("/msgraph/users/" + msUserId + "/memberOf?%24filter=id%20eq%20%27f476f724-350b-4ff4-8e74-141cda9e824e%27&%24count=true&%24select=id")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("nav/msgraph-memberof-egenansatt.json")));
	}

	protected static void stubMsGraphMemberOfNotEgenAnsatt(String msUserId) {
		stubFor(get("/msgraph/users/" + msUserId + "/memberOf?%24filter=id%20eq%20%27f476f724-350b-4ff4-8e74-141cda9e824e%27&%24count=true&%24select=id")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("nav/msgraph-memberof-not-egenansatt.json")));
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

	protected static void stubSak() {
		stubSak("gsak-sakerBySaksId_not_bid-happy.json");
	}

	protected static void stubSak(String filename) {
		stubFor(get("/gsak?aktoerId=" + AKTOER_ID)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("gsak/" + filename)));
	}

	protected static void stubSakOrgnr() {
		stubFor(get("/gsak?orgnr=" + ORG_NR)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("gsak/gsak-sakerBySaksId_not_bid-happy.json")));
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
		stubFor(get(PENSJON_SPRINGAPI_SAK_SAMMENDRAG_URL)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType())
						.withBodyFile("psak/" + filename)));
	}

	protected static void stubBidrag() {
		stubBidrag("bidragsak-happy.json");
	}

	protected static void stubBidrag(String filename) {
		stubFor(get("/bidrag/654321").willReturn(aResponse().withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/" + filename)));
	}

	protected static void abacPermit() {
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected void abacDenyPep6dSkipPep3OrPep2() {
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
				.willSetStateTo(STATE_PEP4));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP4)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP5));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP5)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP6D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP6D)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));
	}

	protected void abacDenyPep6dSkipPep2Pep4Pep5() {
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
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP6D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP6D)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));
	}

	protected void abacDenyPep6dSkipPep2Pep3Pep4Pep5() {
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
				.willSetStateTo(STATE_PEP6D));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP6D)
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

	protected void abacDenyPep5SkipPep2OrPep3() {
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
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP4));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP4)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP5));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP5)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));
	}

	protected void abacDenyPep5SkipPep4() {
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
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP5));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP5)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));
	}

	protected void abacDenyPep5SkipPep2Pep3Pep4() {
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
				.willSetStateTo(STATE_PEP5));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP5)
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

	protected void abacDenyPep5SkipPep1gPep2Pep2dPep3() {
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STARTED)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json"))
				.willSetStateTo(STATE_PEP5));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP5)
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

	protected void abacDenyPep4SkipPep1gPep2Pep2dPep3() {
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
				.willSetStateTo(STATE_PEP4));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP4)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));
	}

	protected void abacDenyPep4SkipPep2OrPep3() {
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
				.willSetStateTo(STATE_PEP4));
		stubFor(post(urlEqualTo("/abac"))
				.inScenario(SCENARIO_ABAC)
				.whenScenarioStateIs(STATE_PEP4)
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

	protected void abacDenyPep3SkipPep2() {
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
						.withBodyFile("abac/abac-deny.json"))
				.willSetStateTo(STATE_PERMIT));
	}

	protected void abacDenyPep3SkipPep2dAndPep2() {
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
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));
	}

	protected void abacDenyPep1g() {
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
			verify(6, postRequestedFor(urlEqualTo("/abac")));
		} else {
			verify(3, postRequestedFor(urlEqualTo("/abac")));
		}
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep3ASkipPep2AndHttpStatusCode(HttpStatusCode expectedHttpStatus, HttpStatusCode actualHttpStatus) {
		verify(3, postRequestedFor(urlEqualTo("/abac")));
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep3SkipPep2AndPep2dAndHttpStatusCode(HttpStatusCode expectedHttpStatus, HttpStatusCode actualHttpStatus) {
		verify(2, postRequestedFor(urlEqualTo("/abac")));
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep4SkipPep2OrPep3AndHttpStatusCode(HttpStatusCode expectedHttpStatus, HttpStatusCode actualHttpStatus) {
		verify(4, postRequestedFor(urlEqualTo("/abac")));
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep5SkipPep2OrPep3AndHttpStatusCode(boolean isDokumentoversikt, HttpStatusCode expectedHttpStatus, HttpStatusCode actualHttpStatus) {
		if (isDokumentoversikt) {
			verify(6, postRequestedFor(urlEqualTo("/abac")));
		} else {
			verify(5, postRequestedFor(urlEqualTo("/abac")));
		}
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep6dSkipPep2AndHttpStatusCode(HttpStatusCode expectedHttpStatus, HttpStatusCode actualHttpStatus) {
		verify(6, postRequestedFor(urlEqualTo("/abac")));
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyabacDenyPep6dSkipPep2Pep3AndHttpStatusCode(HttpStatusCode expectedHttpStatus, HttpStatusCode actualHttpStatus) {
		verify(5, postRequestedFor(urlEqualTo("/abac")));
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

	protected String base64(String journalpostId) {
		if (journalpostId == null) {
			return null;
		}
		return Base64.getEncoder().encodeToString(journalpostId.getBytes());
	}

	@SneakyThrows
	protected String stringFromClasspath(String resourcename) {
		return new String(requireNonNull(this.getClass().getClassLoader().getResourceAsStream(resourcename)).readAllBytes(), StandardCharsets.UTF_8);
	}
}
