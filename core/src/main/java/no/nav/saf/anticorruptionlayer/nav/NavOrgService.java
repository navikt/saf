package no.nav.saf.anticorruptionlayer.nav;

import com.microsoft.graph.models.User;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.config.SafProperties;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class NavOrgService {
	private final MsGraphConsumer msGraphConsumer;
	private final NavHrOrganisasjonConsumer navStatConsumer;
	private final String azureGroupEgenAnsattObjectId;

	public NavOrgService(SafProperties safProperties,
						 MsGraphConsumer msGraphConsumer,
						 NavHrOrganisasjonConsumer navHrOrganisasjonConsumer) {
		this.msGraphConsumer = msGraphConsumer;
		this.navStatConsumer = navHrOrganisasjonConsumer;
		this.azureGroupEgenAnsattObjectId = safProperties.getAzureGroupEgenAnsattObjectId();
	}

	public boolean isNavIdentInEgenAnsattGroup(String navIdent) {
		return isUserInGroup(navIdent, azureGroupEgenAnsattObjectId);
	}

	public boolean isUserInGroup(String navIdent, String azureAdGroup) {
		Optional<User> userOpt = msGraphConsumer.getUser(navIdent);
		return userOpt.filter(user -> msGraphConsumer.isMemberOf(user, azureAdGroup)).isPresent();
	}

	public boolean isOrganisasjonsnummerNavBedrift(String organisasjonsnummer) {
		NavHrOrganisasjonResponse navHrOrganisasjonResponse = navStatConsumer.getNavBedrift(organisasjonsnummer);
		return navHrOrganisasjonResponse.erNavOrgnummer().erNavOrganisasjon();
	}
}
