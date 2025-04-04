package no.nav.saf.anticorruptionlayer.nav;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class NavOrgService {
	private final NavHrOrganisasjonConsumer navHrOrganisasjonConsumer;
	final Set<String> navHrOrganisasjonCache;

	public NavOrgService(NavHrOrganisasjonConsumer navHrOrganisasjonConsumer
	) {
		this.navHrOrganisasjonConsumer = navHrOrganisasjonConsumer;
		this.navHrOrganisasjonCache = new HashSet<>(2000);
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
			log.error("navHrOrganisasjonCache svar fra DVH ORDS var tomt");
		} else {
			log.info("navHrOrganisasjonCache Lastet {} Nav-organisasjoner fra DVH ORDS", response.items().size());
		}
	}
}
