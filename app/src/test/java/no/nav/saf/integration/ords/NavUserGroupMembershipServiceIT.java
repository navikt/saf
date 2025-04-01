package no.nav.saf.integration.ords;

import no.nav.saf.anticorruptionlayer.nav.MsGraphConsumer;
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
	MsGraphConsumer msGraphConsumer;

	@Test
	void shouldLookUpMsGraphMemberOfEgenAnsatt() {
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfSeveralGroups(MS_ID_SAKSBEHANDLER, "nav/msgraph-memberof-egenansatt.json");

		NavUserGroupMembershipService navUserGroupMembershipService = new NavUserGroupMembershipService(safProperties, msGraphConsumer);

		assertThat(navUserGroupMembershipService.isNavIdentInEgenAnsattGroup(NAV_IDENT_SAKSBEHANDLER)).isTrue();
	}

	@Test
	void shouldLookUpMsGraphMemberOfNotEgenAnsatt() {
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfSeveralGroups(MS_ID_SAKSBEHANDLER, "nav/msgraph-memberof-not-egenansatt.json");

		NavUserGroupMembershipService navUserGroupMembershipService = new NavUserGroupMembershipService(safProperties, msGraphConsumer);

		assertThat(navUserGroupMembershipService.isNavIdentInEgenAnsattGroup(NAV_IDENT_SAKSBEHANDLER)).isFalse();
	}
}