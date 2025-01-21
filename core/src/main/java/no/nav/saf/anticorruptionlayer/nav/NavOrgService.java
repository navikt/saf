package no.nav.saf.anticorruptionlayer.nav;

import com.microsoft.graph.models.User;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.config.SafProperties;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;


@Slf4j
@Component
public class NavOrgService {
	private final MsGraphConsumer msGraphConsumer;
	private final NavHrOrganisasjonConsumer navHrOrganisasjonConsumer;
	private final String azureGroupEgenAnsattObjectId;
	final Set<String> navHrOrganisasjonCache;

	public NavOrgService(SafProperties safProperties,
						 MsGraphConsumer msGraphConsumer,
						 NavHrOrganisasjonConsumer navHrOrganisasjonConsumer) {
		this.msGraphConsumer = msGraphConsumer;
		this.azureGroupEgenAnsattObjectId = safProperties.getAzureGroupEgenAnsattObjectId();
		this.navHrOrganisasjonConsumer = navHrOrganisasjonConsumer;
		this.navHrOrganisasjonCache = new HashSet<>(2000);
	}

	public boolean isNavIdentInEgenAnsattGroup(String navIdent) {
		return isUserInGroup(navIdent, azureGroupEgenAnsattObjectId);
	}

	private boolean isUserInGroup(String navIdent, String azureAdGroup) {
		Optional<User> userOpt = msGraphConsumer.getUser(navIdent);
		return userOpt.filter(user -> msGraphConsumer.isMemberOf(user, azureAdGroup)).isPresent();
	}

	public boolean isOrganisasjonsnummerNavBedrift(String organisasjonsnummer) {
		return navHrOrganisasjonCache.contains(organisasjonsnummer);
	}

	@Async
	@EventListener(ContextRefreshedEvent.class)
	public void populateCache() {
		NavHrOrganisasjonORDSResponse response = navHrOrganisasjonConsumer.getAllNavOrganisasjon();
		response.organisasjoner().forEach(org -> navHrOrganisasjonCache.add(org.organisasjonsnummer()));
		if (response.items().isEmpty()) {
			log.error("navHrOrganisasjonCache Response from DVH ORDS was empty list");
		} else {
			log.info("navHrOrganisasjonCache Loaded {} Nav organisations from DVH ORDS", response.items().size());
		}
	}
}
