package no.nav.saf.anticorruptionlayer.nav;

import com.microsoft.graph.models.User;
import no.nav.saf.config.SafProperties;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class NavOrgService {
	private final MsGraphConsumer msGraphConsumer;
	private final NavHrBedriftConsumer navStatConsumer;
	private final String azureGroupEgenAnsattObjectId;

	public NavOrgService(SafProperties safProperties,
						 MsGraphConsumer msGraphConsumer,
						 NavHrBedriftConsumer navHrBedriftConsumer) {
		this.msGraphConsumer = msGraphConsumer;
		this.navStatConsumer = navHrBedriftConsumer;
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
		NavHrBedriftResponse navHrBedriftResponse = navStatConsumer.getNavBedrift(organisasjonsnummer);
		return navHrBedriftResponse.hrNavBedrift().erNavBedrift();
	}
}
