package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;

public abstract class StandardAbacBackedPep<T> extends Pep<T> {
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
	abstract PepAnswer verifyAbacPdpDecision(T ressurs, SafRequestContext safRequestContext);

	@Override
	PepAnswer verifyRestSTSCredentialFlowAccess(T ressurs, SafRequestContext safRequestContext) {
		return verifyAbacPdpDecision(ressurs, safRequestContext);
	}

	@Override
	public PepAnswer hasAccessWithAnswer(T ressurs, SafRequestContext safRequestContext) {
		if (safRequestContext.getSecurityContext().isJwtAzureClientCredentialFlow()) {
			return verifyAzureClientCredentialFlowAccess(ressurs, safRequestContext);
		} else if (safRequestContext.isSystem()) {
			return verifyRestSTSCredentialFlowAccess(ressurs, safRequestContext);
		} else {
			return verifyAbacPdpDecision(ressurs, safRequestContext);
		}
	}

	protected PepAnswer mapToAbacAnswer(XacmlResponse xacmlResponse) {
		return xacmlResponse.isPermit() ? PepAnswer.permit() : translateToDenyReasonCode(xacmlResponse);
	}

	protected abstract PepAnswer translateToDenyReasonCode(XacmlResponse xacmlResponse);

}
