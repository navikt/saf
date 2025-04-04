package no.nav.saf.integration.ords;

import no.nav.saf.anticorruptionlayer.nav.MsGraphEntraGroupMembershipService;
import no.nav.saf.config.SafProperties;
import no.nav.saf.endpoints.AbstractItest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class MsGraphEntraGroupMembershipServiceIT extends AbstractItest {

	@Autowired
	SafProperties safProperties;
	@Autowired
	MsGraphEntraGroupMembershipService msGraphEntraGroupMembershipService;

	@Test
	void shouldLookUpAMsGraphMemberOfEgenAnsattOnly() {
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfSeveralGroups(MS_ID_SAKSBEHANDLER, "nav/msgraph-memberof-egenansatt.json");

		assertThat(msGraphEntraGroupMembershipService.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getEgenAnsattObjectId())).isTrue();
		assertThat(msGraphEntraGroupMembershipService.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getFortroligAdresseObjectId())).isFalse();
		assertThat(msGraphEntraGroupMembershipService.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getStrengtFortroligAdresseObjectId())).isFalse();
		verifyMsGraphMemberOfSeveralGroupsCalled(MS_ID_SAKSBEHANDLER, 1);
	}

	@Test
	void shouldLookUpAndCacheMsGraphMemberOfNoGroups() {
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfSeveralGroups(MS_ID_SAKSBEHANDLER, "nav/msgraph-memberof-not-egenansatt.json");

		assertThat(msGraphEntraGroupMembershipService.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getEgenAnsattObjectId())).isFalse();
		assertThat(msGraphEntraGroupMembershipService.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getFortroligAdresseObjectId())).isFalse();
		assertThat(msGraphEntraGroupMembershipService.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getStrengtFortroligAdresseObjectId())).isFalse();
		verifyMsGraphMemberOfSeveralGroupsCalled(MS_ID_SAKSBEHANDLER, 1);
	}

	@Test
	void shouldLookUpAndCacheAllRelevantGroupsForUser() {
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfSeveralGroups(MS_ID_SAKSBEHANDLER, "nav/msgraph-memberof-egenansatt-fortrolig-strengt-fortrolig.json");

		assertThat(msGraphEntraGroupMembershipService.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getEgenAnsattObjectId())).isTrue();
		assertThat(msGraphEntraGroupMembershipService.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getFortroligAdresseObjectId())).isTrue();
		assertThat(msGraphEntraGroupMembershipService.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getStrengtFortroligAdresseObjectId())).isTrue();
		verifyMsGraphMemberOfSeveralGroupsCalled(MS_ID_SAKSBEHANDLER, 1);
	}
}