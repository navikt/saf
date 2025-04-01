package no.nav.saf.anticorruptionlayer.nav;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.config.SafProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NavUserGroupMembershipService {
	private final MsGraphConsumer msGraphConsumer;
	private final SafProperties.AzureGroup azureGroups;

	public NavUserGroupMembershipService(SafProperties safProperties,
										 MsGraphConsumer msGraphConsumer
	) {
		this.msGraphConsumer = msGraphConsumer;
		this.azureGroups = safProperties.getAzureGroup();
	}

	public boolean isNavIdentInEgenAnsattGroup(String navIdent) {
		return msGraphConsumer.isUserInGroup(navIdent, azureGroups.getEgenAnsattObjectId());
	}
}
