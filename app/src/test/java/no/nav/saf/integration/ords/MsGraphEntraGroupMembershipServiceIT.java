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
		stubMsGraphMemberOfEgenAnsattDefaultSaksbehandler();

		assertThat(msGraphEntraGroupMembershipService.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getEgenAnsattObjectId())).isTrue();
		assertThat(msGraphEntraGroupMembershipService.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getJoarkVedlikeholdObjectId())).isFalse();
		assertThat(msGraphEntraGroupMembershipService.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getJoarkHistoriskObjectId())).isFalse();
		verifyMsGraphMemberOfSeveralGroupsCalled(MS_ID_SAKSBEHANDLER, 1);
	}

	@Test
	void shouldLookUpAndCacheMsGraphMemberOfNoGroups() {
		stubMsGraphMemberOfNoGroupsDefaultSaksbehandler();

		assertThat(msGraphEntraGroupMembershipService.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getEgenAnsattObjectId())).isFalse();
		assertThat(msGraphEntraGroupMembershipService.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getJoarkVedlikeholdObjectId())).isFalse();
		assertThat(msGraphEntraGroupMembershipService.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getJoarkHistoriskObjectId())).isFalse();
		verifyMsGraphMemberOfSeveralGroupsCalled(MS_ID_SAKSBEHANDLER, 1);
	}

	@Test
	void shouldLookUpAndCacheAllRelevantGroupsForUser() {
		stubMsGraphMemberOfAllRelevantGroupsDefaultSaksbehandler();

		assertThat(msGraphEntraGroupMembershipService.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getEgenAnsattObjectId())).isTrue();
		assertThat(msGraphEntraGroupMembershipService.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getJoarkVedlikeholdObjectId())).isTrue();
		assertThat(msGraphEntraGroupMembershipService.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getJoarkHistoriskObjectId())).isTrue();
		assertThat(msGraphEntraGroupMembershipService.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getLeseUtgaatteDokumenterObjectId())).isTrue();
		verifyMsGraphMemberOfSeveralGroupsCalled(MS_ID_SAKSBEHANDLER, 1);
	}
}