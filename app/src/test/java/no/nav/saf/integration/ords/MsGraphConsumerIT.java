package no.nav.saf.integration.ords;

import no.nav.saf.anticorruptionlayer.nav.MsGraphConsumer;
import no.nav.saf.config.SafProperties;
import no.nav.saf.endpoints.AbstractItest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

class MsGraphConsumerIT extends AbstractItest {

	@Autowired
	SafProperties safProperties;
	@Autowired
	MsGraphConsumer msGraphConsumer;

	@Test
	void shouldLookUpAMsGraphMemberOfEgenAnsattOnly() {
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfSeveralGroups(MS_ID_SAKSBEHANDLER, "nav/msgraph-memberof-egenansatt.json");

		assertThat(msGraphConsumer.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getEgenAnsattObjectId())).isTrue();
		assertThat(msGraphConsumer.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getFortroligAdresseObjectId())).isFalse();
		assertThat(msGraphConsumer.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getStrengtFortroligAdresseObjectId())).isFalse();
		verifyMsGraphMemberOfSeveralGroupsCalled(MS_ID_SAKSBEHANDLER, 1);
	}

	@Test
	void shouldLookUpAndCacheMsGraphMemberOfNoGroups() {
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfSeveralGroups(MS_ID_SAKSBEHANDLER, "nav/msgraph-memberof-not-egenansatt.json");

		assertThat(msGraphConsumer.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getEgenAnsattObjectId())).isFalse();
		assertThat(msGraphConsumer.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getFortroligAdresseObjectId())).isFalse();
		assertThat(msGraphConsumer.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getStrengtFortroligAdresseObjectId())).isFalse();
		verifyMsGraphMemberOfSeveralGroupsCalled(MS_ID_SAKSBEHANDLER, 1);
	}

	@Test
	void shouldLookUpAndCacheAllRelevantGroupsForUser() {
		stubMsGraphGetUser(NAV_IDENT_SAKSBEHANDLER);
		stubMsGraphMemberOfSeveralGroups(MS_ID_SAKSBEHANDLER, "nav/msgraph-memberof-egenansatt-fortrolig-strengt-fortrolig.json");

		assertThat(msGraphConsumer.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getEgenAnsattObjectId())).isTrue();
		assertThat(msGraphConsumer.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getFortroligAdresseObjectId())).isTrue();
		assertThat(msGraphConsumer.isUserInGroup(NAV_IDENT_SAKSBEHANDLER, safProperties.getAzureGroup().getStrengtFortroligAdresseObjectId())).isTrue();
		verifyMsGraphMemberOfSeveralGroupsCalled(MS_ID_SAKSBEHANDLER, 1);
	}
}