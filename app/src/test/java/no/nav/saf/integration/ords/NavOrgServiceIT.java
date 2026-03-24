package no.nav.saf.integration.ords;

import no.nav.saf.anticorruptionlayer.nav.NavHrOrganisasjonConsumer;
import no.nav.saf.anticorruptionlayer.nav.NavOrgService;
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
	NavHrOrganisasjonConsumer navHrOrganisasjonConsumer;

	@Test
	void shouldLoadNavOrgsOrgnrFromORDSAndCacheThemLocally() {
		stubNavHrOrgOrds();

		NavOrgService navOrgService = new NavOrgService(navHrOrganisasjonConsumer);
		navOrgService.populateCache();

		assertThat(navOrgService.isOrganisasjonsnummerNavBedrift(ORGANISASJONSNUMMER_NAV_ORG)).isTrue();
	}

	private static void stubNavHrOrgOrds() {
		stubFor(get("/hrnavorganisasjon/ords/dvh/dt_hr/nav_organisasjon_orgnummer?limit=2000")
				.willReturn(aResponse().withStatus(OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("nav/ords_response.json")));
	}
}