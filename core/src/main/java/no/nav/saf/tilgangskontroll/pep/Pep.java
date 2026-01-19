package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.reasons.DenyReason;

/**
 * Policy Enforcement Point
 * <p>
 * Evaluerer tilgang til en ressurs T.
 */
@Slf4j
public abstract class Pep<T> {

	/**
	 * Sjekker tilgang for app registration autentisert med client credential flow i Azure.
	 * Implementerer:
	 * https://confluence.adeo.no/display/BOA/saf+-+Tilgangskontroll#safTilgangskontroll-Tilgangsreglerforservicebrukerautentisertmedclient-credential-flowiAzure
	 *
	 * @param ressurs           Ressursen som skal sjekkes
	 * @param safRequestContext Kontekst for kallet
	 * @return Beslutning om tilgang fra intern PDP
	 */
	abstract PepAnswer verifyAzureClientCredentialFlowAccess(T ressurs, SafRequestContext safRequestContext);

	/**
	 * Sjekker tilgang for app registration autentisert med Rest-STS-token (systembrukere som ikke går via azure-flow)
	 * NB: denne skal alltid gi samme resultat som verifyAzureClientCredentialFlowAccess for et gitt real-world system,
	 * men bruker data fra andre kilder
	 *
	 * @param ressurs           Ressursen som skal sjekkes
	 * @param safRequestContext Kontekst for kallet
	 * @return Beslutning om tilgang fra intern PDP
	 */
	abstract PepAnswer verifyRestSTSCredentialFlowAccess(T ressurs, SafRequestContext safRequestContext);

	public boolean hasAccess(T ressurs, SafRequestContext safRequestContext) {
		return hasAccessWithAnswer(ressurs, safRequestContext).isPermit();
	}

	public PepAnswer hasAccessWithAnswer(T ressurs, SafRequestContext safRequestContext) {
		if (safRequestContext.getSecurityContext().isJwtAzureClientCredentialFlow()) {
			return verifyAzureClientCredentialFlowAccess(ressurs, safRequestContext);
		} else if (safRequestContext.isSystem()) {
			return verifyRestSTSCredentialFlowAccess(ressurs, safRequestContext);
		} else {
			// Denne må implementeres fordi det er forskjellig for Abac-backed PEP og MsGraph-backed PEP
			throw new UnsupportedOperationException("Not implemented!");
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

	void logDeny(PepAnswer pepAnswer) {
		log.info("SAF tilgangskontroll har avvist tilgang til ressurs med begrunnelse={}",
				pepAnswer.getPepDenyReason() != null ? pepAnswer.getPepDenyReason().getHumanReadableDenyReason() : "Ukjent");
	}
}
