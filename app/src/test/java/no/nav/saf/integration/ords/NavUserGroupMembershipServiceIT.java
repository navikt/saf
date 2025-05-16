package no.nav.saf.integration.ords;

import no.nav.saf.anticorruptionlayer.nav.MsGraphEntraGroupMembershipService;
import no.nav.saf.anticorruptionlayer.nav.NavUserGroupMembershipService;
import no.nav.saf.config.SafProperties;
import no.nav.saf.endpoints.AbstractItest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class NavUserGroupMembershipServiceIT extends AbstractItest {

	@Autowired
	SafProperties safProperties;
	@Autowired
	MsGraphEntraGroupMembershipService msGraphEntraGroupMembershipService;

	@Test
	void shouldLookUpMsGraphMemberOfEgenAnsatt() {
		stubMsGraphMemberOfEgenAnsattDefaultSaksbehandler();

		NavUserGroupMembershipService navUserGroupMembershipService = new NavUserGroupMembershipService(safProperties, msGraphEntraGroupMembershipService);

		assertThat(navUserGroupMembershipService.isNavIdentInEgenAnsattGroup(NAV_IDENT_SAKSBEHANDLER)).isTrue();
		verifyMsGraphMemberOfSeveralGroupsCalled(MS_ID_SAKSBEHANDLER, 1);
	}

	@Test
	void shouldLookUpMsGraphMemberOfNotEgenAnsatt() {
		stubMsGraphMemberOfNoGroupsDefaultSaksbehandler();

		NavUserGroupMembershipService navUserGroupMembershipService = new NavUserGroupMembershipService(safProperties, msGraphEntraGroupMembershipService);

		assertThat(navUserGroupMembershipService.isNavIdentInEgenAnsattGroup(NAV_IDENT_SAKSBEHANDLER)).isFalse();
		verifyMsGraphMemberOfSeveralGroupsCalled(MS_ID_SAKSBEHANDLER, 1);
	}
}