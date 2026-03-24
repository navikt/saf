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
	static final int NAV_ORGANISASJONER_LIMIT = 2000;

	private final NavHrOrganisasjonConsumer navHrOrganisasjonConsumer;
	private final Set<String> navHrOrganisasjonCache;

	public NavOrgService(NavHrOrganisasjonConsumer navHrOrganisasjonConsumer
	) {
		this.navHrOrganisasjonConsumer = navHrOrganisasjonConsumer;
		this.navHrOrganisasjonCache = new HashSet<>(NAV_ORGANISASJONER_LIMIT);
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
			log.error("navHrOrganisasjonCache svar fra DVH ORDS var tomt.");
		} else if (response.items().size() == NAV_ORGANISASJONER_LIMIT) {
			log.error("navHrOrganisasjonCache lastet {} Nav-organisasjoner fra DVH ORDS. Respons fra DVH er lik limit. Det kan hende det er mer data som ikke blir lest inn.", response.items().size());
		} else {
			log.info("navHrOrganisasjonCache lastet {} Nav-organisasjoner fra DVH ORDS", response.items().size());
		}
	}
}
