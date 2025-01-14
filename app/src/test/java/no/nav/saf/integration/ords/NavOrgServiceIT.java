package no.nav.saf.integration.ords;

import no.nav.saf.anticorruptionlayer.nav.MsGraphConsumer;
import no.nav.saf.anticorruptionlayer.nav.NavHrOrganisasjonConsumer;
import no.nav.saf.anticorruptionlayer.nav.NavOrgService;
import no.nav.saf.config.SafProperties;
import no.nav.saf.endpoints.AbstractItest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class NavOrgServiceIT extends AbstractItest {

	public static final String ORGANISASJONSNUMMER_NAV_ORG = "832715182";
	@Autowired
	SafProperties safProperties;
	@Autowired
	MsGraphConsumer msGraphConsumer;
	@Autowired
	NavHrOrganisasjonConsumer navHrOrganisasjonConsumer;

	@Test
	void shouldLoadNavOrgs() {
		stubNavHrOrgOrds();

		NavOrgService navOrgService = new NavOrgService(safProperties, msGraphConsumer, navHrOrganisasjonConsumer);
		navOrgService.populateCache();

		assertThat(navOrgService.isOrganisasjonsnummerNavBedrift(ORGANISASJONSNUMMER_NAV_ORG)).isTrue();
	}

	@Test
	void shouldLookUpMsGraphMemberOfEgenAnsatt() {
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfEgenAnsatt(MS_ID_SAKSBEHANDLER);

		NavOrgService navOrgService = new NavOrgService(safProperties, msGraphConsumer, navHrOrganisasjonConsumer);

		assertThat(navOrgService.isNavIdentInEgenAnsattGroup(NAV_IDENT_SAKSBEHANDLER)).isTrue();
	}

	@Test
	void shouldLookUpMsGraphMemberOfNotEgenAnsatt() {
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfNotEgenAnsatt(MS_ID_SAKSBEHANDLER);

		NavOrgService navOrgService = new NavOrgService(safProperties, msGraphConsumer, navHrOrganisasjonConsumer);

		assertThat(navOrgService.isNavIdentInEgenAnsattGroup(NAV_IDENT_SAKSBEHANDLER)).isFalse();
	}

	private static void stubNavHrOrgOrds() {
		stubFor(get("/hrnavorganisasjon/ords/dvh/dt_hr/nav_organisasjon_orgnummer")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("nav/ords_response.json")));
	}

	protected static void stubMsGraphGetUser(String navIdent) {
		stubFor(get("/msgraph/users?$count=true&$filter=onPremisesSamAccountName%20eq%20%27" + navIdent + "%27&$select=id")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("nav/msgraph-users.json")));
	}

	protected static void stubMsGraphMemberOfEgenAnsatt(String msUserId) {
		stubMsGraphMemberOf(msUserId, "nav/msgraph-memberof-egenansatt.json");
	}

	protected static void stubMsGraphMemberOfNotEgenAnsatt(String msUserId) {
		stubMsGraphMemberOf(msUserId, "nav/msgraph-memberof-not-egenansatt.json");
	}

	protected static void stubMsGraphMemberOf(String msUserId, String bodyFile) {
		stubFor(get("/msgraph/users/" + msUserId + "/memberOf?$count=true&$filter=id%20eq%20%27f476f724-350b-4ff4-8e74-141cda9e824e%27&$select=id")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile(bodyFile)));
	}
}