package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.tilgangskontroll.SafRequestContext;

/**
 * Policy Enforcement Point for ABAC.
 * <p>
 * Evaluerer tilgang til en ressurs T.
 */
@Slf4j
public abstract class Pep<T> {
	/**
	 * Kall mot abac-saf (ekstern tjeneste) som er Policy Decision Point (PDP).
	 * Bestemmer om kall skal få tilgang til ressurs.
	 * Implementerer:
	 * https://confluence.adeo.no/display/BOA/saf+-+Tilgangskontroll#safTilgangskontroll-TilgangsreglerforNAV-ansatte
	 * https://confluence.adeo.no/display/BOA/saf+-+Tilgangskontroll#safTilgangskontroll-Tilgangsreglerforservicebruker
	 *
	 * @param ressurs           Ressursen som skal sjekkes
	 * @param safRequestContext Kontekst for kallet
	 * @return Beslutning om tilgang fra saf-abac PDP
	 */
	abstract AbacAnswer verifyAbacPdpDecision(T ressurs, SafRequestContext safRequestContext);

	/**
	 * Sjekker tilgang for app registration autentisert med client credential flow i Azure.
	 * Implementerer:
	 * https://confluence.adeo.no/display/BOA/saf+-+Tilgangskontroll#safTilgangskontroll-Tilgangsreglerforservicebrukerautentisertmedclient-credential-flowiAzure
	 *
	 * @param ressurs           Ressursen som skal sjekkes
	 * @param safRequestContext Kontekst for kallet
	 * @return Beslutning om tilgang fra intern ABAC PDP
	 */
	abstract AbacAnswer verifyAzureClientCredentialFlowAccess(T ressurs, SafRequestContext safRequestContext);

	public boolean hasAccess(T ressurs, SafRequestContext safRequestContext) {
		return hasAccessWithAnswer(ressurs, safRequestContext).isPermit();
	}

	public AbacAnswer hasAccessWithAnswer(T ressurs, SafRequestContext safRequestContext) {
		if (safRequestContext.getSecurityContext().isJwtAzureClientCredentialFlow()) {
			return verifyAzureClientCredentialFlowAccess(ressurs, safRequestContext);
		} else {
			return verifyAbacPdpDecision(ressurs, safRequestContext);
		}
	}

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
}
