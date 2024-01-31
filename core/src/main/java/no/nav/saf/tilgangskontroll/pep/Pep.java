package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;

import static no.nav.saf.tilgangskontroll.pep.AbacAnswer.deny;

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
			// return mapXacmlResponse(response);
		}
	}

	protected boolean decide(Decision decision) {
		return Decision.PERMIT.equals(decision);
	}

	protected boolean decide(AbacAnswer.AbacDecision decision) {
		return AbacAnswer.AbacDecision.PERMIT.equals(decision);
	}

	protected AbacAnswer mapXacmlResponse(XacmlResponse xacmlResponse) {
		return xacmlResponse.isPermit() ? AbacAnswer.permit() : AbacAnswer.deny(translateToDenyReasonCode(xacmlResponse));
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

	abstract AbacAnswer.AbacDenyReasonCode translateToDenyReasonCode(XacmlResponse xacmlResponse);

}
