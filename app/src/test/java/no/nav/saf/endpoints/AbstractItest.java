package no.nav.saf.endpoints;

import lombok.SneakyThrows;
import no.nav.saf.ApplicationConfig;
import no.nav.saf.anticorruptionlayer.nav.NavOrgService;
import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.endpoints.testconfig.ValkeyCacheTestConfig;
import no.nav.security.mock.oauth2.MockOAuth2Server;
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback;
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.wiremock.spring.EnableWireMock;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostMapper.SKJULT_TITTEL;
import static no.nav.saf.headers.NavHeaders.NAV_USER_ID;
import static no.nav.saf.tilgangskontroll.SafSecurityContext.TILGANG_NAV_USERID_HEADER_ROLE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.MULTI_STATUS;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(classes = {AbstractItest.TestConfig.class, ValkeyCacheTestConfig.class, ApplicationConfig.class},
		webEnvironment = RANDOM_PORT,
		properties = {"spring.main.allow-bean-definition-overriding=true"})
@AutoConfigureTestRestTemplate
@ActiveProfiles(value = {"itest", "wiremock"})
@EnableMockOAuth2Server
@EnableWireMock
public abstract class AbstractItest {

	protected static final String NAV_IDENT_SAKSBEHANDLER = "Z123456";
	protected static final String MS_ID_SAKSBEHANDLER = "11111111-2222-3333-4444-555555555555";
	protected static final String ORG_NR = "894705922";
	protected static final String AKTOER_ID = "1912374211459";
	protected static final String PENSJON_API_SAK_SAMMENDRAG_URL = "/pensjon/api/sak/sammendrag";
	protected static final String PENSJON_API_PIP_HENT_BRUKER_OG_ENHETSTILGANGER_FOR_SAK_V1_URL = "/pensjon/api/pip/hentBrukerOgEnhetstilgangerForSak/v1";
	protected static final String SAK_API_PATH_MED_QUERY_PARA_AKTOER_ID = "/sak?aktoerId=" + AKTOER_ID;
	protected static final String SAK_API_PATH_MED_QUERY_PARA_FAGSAK_NR = "/sak?fagsakNr=ARENA-1&applikasjon=AO01";

	private static final String ENTRA_PROXY_ANSATT_PATH = "/entra-proxy/api/v1/tema/ansatt/.*";
	protected static final String BISYS_FORELDRESKAP_SAK_ID = "201545004";
	protected static final String BISYS_BIDRAG_SAK_ID = "654321";

	@Configuration
	public static class TestConfig {

		@Bean
		@Primary
		ClientHttpRequestFactory clientHttpRequestFactoryTest() {
			return new SimpleClientHttpRequestFactory();
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
	@Autowired
	protected JsonMapper jsonMapper;

	@AfterEach
	void resetCache() {
		stringRedisTemplate.getConnectionFactory().getConnection().serverCommands().flushDb();
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
		headers.set(NAV_USER_ID, NAV_IDENT_SAKSBEHANDLER);
		return headers;
	}

	protected HttpHeaders createHeadersNavUserIdWithoutRoles() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(APPLICATION_JSON);
		headers.setBearerAuth(getClientCredentialTokenWithoutRoles());
		headers.set(NAV_USER_ID, NAV_IDENT_SAKSBEHANDLER);
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

	private String getClientCredentialToken() {
		return getClientCredentialTokenWithRoles(List.of("dokument_tema_hje", TILGANG_NAV_USERID_HEADER_ROLE));
	}

	private String getClientCredentialTokenWithRoles(List<String> roles) {
		String oidSubEqual = UUID.randomUUID().toString();
		return jwt("dev-itest:isa:gosys",
				Map.of(
						"oid", oidSubEqual,
						"sub", oidSubEqual,
						"azp_name", "dev-itest:isa:gosys",
						"roles", roles
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
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
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
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("sak/sak-sakerBySaksId_not_bid-happy.json")));
	}

	protected static void stubFinnjournalposter() {
		stubFinnjournalposter("finnjournalposter-happy.json");
	}

	protected static void stubFinnjournalposter(String filename) {
		stubFor(post("/hentjournalsakinfo/finnjournalposter")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("joark/" + filename)));
	}

	protected static void stubPensjonSakSammendrag() {
		stubPensjonSakSammendrag("psak-hentSakSammendragListe-happy.json");
	}

	protected static void stubPensjonSakSammendrag(String filename) {
		stubFor(get(PENSJON_API_SAK_SAMMENDRAG_URL)
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
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

	protected static void stubBidragForeldreskap() {
		stubBidrag(BISYS_FORELDRESKAP_SAK_ID, "bidragsak-happy.json");
	}

	protected static void stubBidrag(String filename) {
		stubBidrag(BISYS_BIDRAG_SAK_ID, filename);
	}

	protected static void stubBidrag(String sakId, String filename) {
		stubFor(get("/bidrag/v2/pip/sak/" + sakId).willReturn(aResponse().withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("bidrag/" + filename)));
	}

	private static void stubTexasExchangeOboToken() {
		stubFor(post("/nais/token/exchange").willReturn(aResponse().withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("texas/texas_happy.json")));
	}

	public static void stubTexasToken() {
		stubFor(post("/nais/token").willReturn(aResponse().withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("texas/texas_happy.json")));
	}

	protected static void stubTilgangsmaskinenPermit() {
		stubTexasToken();
		stubTilgangsmaskinenPep1gPermit();
		stubTilgangsmaskinenPep3BulkPermit();
	}

	protected static void stubTilgangsmaskinenPep1gPermit() {
		stubTexasToken();
		stubFor(post(urlMatching("/tilgangsmaskinen/api/v1/ccf/komplett/.*")).willReturn(aResponse().withStatus(NO_CONTENT.value())));
	}

	protected static void stubTilgangsmaskinenPep1gDeny() {
		stubTexasToken();
		stubFor(post(urlMatching("/tilgangsmaskinen/api/v1/ccf/komplett/.*")).willReturn(aResponse().withStatus(FORBIDDEN.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("tilgangsmaskinen/" + "tilgangsmaskinen_deny_fortrolig.json")));
	}

	protected static void stubTilgangsmaskinenPep3BulkPermit() {
		stubTexasToken();
		stubFor(post(urlMatching("/tilgangsmaskinen/api/v1/bulk/ccf/.*/KJERNE_REGELTYPE")).willReturn(aResponse().withStatus(MULTI_STATUS.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("tilgangsmaskinen/tilgangsmaskinen_permit_bulk.json")));
	}

	protected static void stubTilgangsmaskinenPep3BulkDeny() {
		stubTexasToken();
		stubTilgangsmaskinenPep1gPermit();
		stubFor(post(urlMatching("/tilgangsmaskinen/api/v1/bulk/ccf/.*/KJERNE_REGELTYPE")).willReturn(aResponse().withStatus(MULTI_STATUS.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("tilgangsmaskinen/tilgangsmaskinen_deny_fortrolig_bulk.json")));
	}

	protected static void stubEntraProxy() {
		stubFor(get(urlMatching(ENTRA_PROXY_ANSATT_PATH)).willReturn(aResponse().withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("entraproxy/tematilganger.json")));
	}

	protected static void stubEntraProxyDeny() {
		stubFor(get(urlMatching(ENTRA_PROXY_ANSATT_PATH)).willReturn(aResponse().withStatus(OK.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBody("[]")));
	}

	protected static void stubTilgangsmaskinenPep7BulkDeny() {
		stubTexasToken();
		stubTilgangsmaskinenPep1gPermit();
		stubTilgangsmaskinenPep3BulkPermit();
		stubFor(post(urlMatching("/tilgangsmaskinen/api/v1/bulk/ccf/.*/KJERNE_REGELTYPE")).willReturn(aResponse().withStatus(MULTI_STATUS.value())
				.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
				.withBodyFile("tilgangsmaskinen/tilgangsmaskinen_deny_fortrolig_bulk.json")));
	}

	protected static void tilgangskontrollPermit() {
		stubEntraProxy();
		stubMsGraphMemberOfAllRelevantGroupsDefaultSaksbehandler();
		stubTilgangsmaskinenPermit();
	}

	protected void tilgangskontrollDenyPep1g() {
		stubTilgangsmaskinenPep1gDeny();
	}

	protected void tilgangskontrollDenyPep2() {
		stubTilgangsmaskinenPermit();
		stubEntraProxyDeny();
	}

	protected void tilgangskontrollDenyPep2d() {
		stubTilgangsmaskinenPermit();
		stubEntraProxyDeny();
		stubMsGraphMemberOfAllRelevantGroupsDefaultSaksbehandler();
	}

	protected void tilgangskontrollDenyPep3() {
		stubTilgangsmaskinenPep1gPermit();
		stubEntraProxy();
		stubTilgangsmaskinenPep3BulkDeny();
		stubMsGraphMemberOfNoGroupsDefaultSaksbehandler();
	}

	protected void tilgangskontrollDenyPep4() {
		stubTilgangsmaskinenPermit();
		stubEntraProxy();
		stubMsGraphMemberOfNoGroupsDefaultSaksbehandler();
	}

	protected void tilgangskontrollDenyPep5() {
		stubTilgangsmaskinenPermit();
		stubEntraProxy();
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfSeveralGroups(MS_ID_SAKSBEHANDLER, "nav/msgraph-checkmembergroup-ikke-joarkvedlikehold.json");
	}

	protected void tilgangskontrollDenyPep6dWithSkjerming() {
		stubTilgangsmaskinenPermit();
		stubEntraProxy();
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfSeveralGroups(MS_ID_SAKSBEHANDLER, "nav/msgraph-checkmembergroup-egenansatt-fortrolig-strengt-fortrolig.json");
	}

	protected void tilgangskontrollDenyPep6d() {
		stubTilgangsmaskinenPermit();
		stubEntraProxy();
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfSeveralGroups(MS_ID_SAKSBEHANDLER, "nav/msgraph-checkmembergroup-ikke-joarkvedlikehold.json");
	}

	protected void tilgangskontrollDenyPep7d() {
		stubEntraProxy();
		stubTilgangsmaskinenPep7BulkDeny();
	}

	protected static void tilgangskontrollDenyPep8d() {
		stubTilgangsmaskinenPermit();
		stubEntraProxy();
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfSeveralGroups(MS_ID_SAKSBEHANDLER, "nav/msgraph-checkmembergroup-ikke-joark-historisk.json");
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

	protected void verifyTilgangsmaskinenDenyPep1gAndHttpStatusCode(HttpStatusCode expectedHttpStatus, HttpStatusCode actualHttpStatus) {
		verify(1, postRequestedFor(urlMatching("/tilgangsmaskinen/api/v1/ccf/komplett/.*")));
		assertEquals(expectedHttpStatus, actualHttpStatus);
	}

	protected void verifyTilgangsmaskinenDenyPep3AndHttpStatusCode(HttpStatusCode actualHttpStatus) {
		verify(1, postRequestedFor(urlMatching("/tilgangsmaskinen/api/v1/bulk/ccf/.*/KJERNE_REGELTYPE")));
		assertEquals(OK, actualHttpStatus);
	}

	protected static void verifyEntraProxyCalled(int count) {
		verify(count, getRequestedFor(urlMatching(ENTRA_PROXY_ANSATT_PATH)));
	}

	protected void verifyDenyPep2(HttpStatusCode actualHttpStatus) {
		verifyEntraProxyCalled(1);
		assertEquals(OK, actualHttpStatus);
	}

	protected void verifyDenyPep2d(HttpStatusCode actualHttpStatus) {
		verifyEntraProxyCalled(1);
		verifyMsGraphMemberOfSeveralGroupsCalled(MS_ID_SAKSBEHANDLER, 1);
		assertEquals(OK, actualHttpStatus);
	}

	protected void verifyDenyPep4(HttpStatusCode actualHttpStatus, int count) {
		verifyEntraProxyCalled(count);
		verifyMsGraphMemberOfSeveralGroupsCalled(MS_ID_SAKSBEHANDLER, 1);
		assertEquals(OK, actualHttpStatus);
	}

	protected void verifyDenyPep5(HttpStatusCode actualHttpStatus, int count) {
		verifyEntraProxyCalled(count);
		verifyMsGraphMemberOfSeveralGroupsCalled(MS_ID_SAKSBEHANDLER, 1);
		assertEquals(OK, actualHttpStatus);
	}

	protected void verifyDenyPep6d(HttpStatusCode actualHttpStatus, int count) {
		verifyEntraProxyCalled(count);
		verifyMsGraphMemberOfSeveralGroupsCalled(MS_ID_SAKSBEHANDLER, 1);
		assertEquals(OK, actualHttpStatus);
	}

	protected void verifyDenyPep7d(HttpStatusCode actualHttpStatus) {
		verifyEntraProxyCalled(1);
		verifyMsGraphMemberOfSeveralGroupsCalled(MS_ID_SAKSBEHANDLER, 0);
		assertEquals(OK, actualHttpStatus);
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
