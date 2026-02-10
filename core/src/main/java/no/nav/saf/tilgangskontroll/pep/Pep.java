package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.tilgangskontroll.SafRequestContext;

/**
 * Policy Enforcement Point
 * <p>
 * Evaluerer tilgang til en ressurs T.
 */
@Slf4j
public abstract class Pep<T> {

	/**
	 * Sjekker tilgang for app autorisert med maskin-til-maskin token
	 * Implementerer:
	 * https://confluence.adeo.no/display/BOA/saf+-+Tilgangskontroll#safTilgangskontroll-Tilgangsreglerforservicebrukerautentisertmedclient-credential-flowiAzure
	 *
	 * @param ressurs           Ressursen som skal sjekkes
	 * @param safRequestContext Kontekst for kallet
	 * @return Beslutning om tilgang fra intern PDP
	 */
	abstract PepAnswer verifyAccessForSystem(T ressurs, SafRequestContext safRequestContext);

	public boolean hasAccess(T ressurs, SafRequestContext safRequestContext) {
		return hasAccessWithAnswer(ressurs, safRequestContext).isPermit();
	}

	public abstract PepAnswer hasAccessWithAnswer(T ressurs, SafRequestContext safRequestContext);

	void traceLogPepStarted(String pepName, Object ressurs) {
		if (log.isTraceEnabled()) {
			log.trace("{} evaluerer ressurs={}", pepName, ressurs);
		}
	}

	void traceLogPepFinished(String pepName, Object ressurs) {
		if (log.isTraceEnabled()) {
			log.trace("{} ferdig evaluert ressurs={}", pepName, ressurs);
		}
	}

	void logDeny(PepAnswer pepAnswer) {
		log.info("SAF tilgangskontroll har avvist tilgang til ressurs med begrunnelse={}",
				pepAnswer.getPepDenyReason() != null ? pepAnswer.getPepDenyReason().getHumanReadableDenyReason() : "Ukjent");
	}
}
