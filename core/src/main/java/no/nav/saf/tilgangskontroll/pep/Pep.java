package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Policy Enforcement Point for ABAC.
 * <p>
 * Evaluerer tilgang til en ressurs T.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface Pep<T> {
	Logger log = LoggerFactory.getLogger(Pep.class);

	boolean hasAccess(T ressurs, SafRequestContext safRequestContext);

	static void traceLogPepStarted(String pepName, Object ressurs) {
		if (log.isTraceEnabled()) {
			log.trace("{} evaluerer TilgangJournalpost={}", pepName, ressurs.toString());
		}
	}

	static void traceLogPepFinished(String pepName, Object ressurs) {
		if (log.isTraceEnabled()) {
			log.trace("{} ferdig evaluert TilgangJournalpost={}", pepName, ressurs.toString());
		}
	}

}
