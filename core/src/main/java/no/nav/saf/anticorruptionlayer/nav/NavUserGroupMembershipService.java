package no.nav.saf.anticorruptionlayer.nav;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.config.SafProperties;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NavUserGroupMembershipService {
	private final MsGraphEntraGroupMembershipService msGraphEntraGroupMembershipService;
	private final SafProperties.AzureGroup azureGroups;

	public NavUserGroupMembershipService(SafProperties safProperties,
										 MsGraphEntraGroupMembershipService msGraphEntraGroupMembershipService
	) {
		this.msGraphEntraGroupMembershipService = msGraphEntraGroupMembershipService;
		this.azureGroups = safProperties.getAzureGroup();
	}

	public boolean isNavIdentInEgenAnsattGroup(String navIdent) {
		return msGraphEntraGroupMembershipService.isUserInGroup(navIdent, azureGroups.getEgenAnsattObjectId());
	}

	public boolean isNavIdentInJoarkVedlikeholdGroup(String navIdent) {
		return msGraphEntraGroupMembershipService.isUserInGroup(navIdent, azureGroups.getJoarkVedlikeholdObjectId());
	}

	public boolean isNavIdentInLeseUtgaatteDokumenterGroup(String navIdent) {
		return msGraphEntraGroupMembershipService.isUserInGroup(navIdent, azureGroups.getLeseUtgaatteDokumenterObjectId());
	}

	public boolean isNavIdentInJoarkHistoriskGroup(String navIdent) {
		return msGraphEntraGroupMembershipService.isUserInGroup(navIdent, azureGroups.getJoarkHistoriskObjectId());
	}
}
