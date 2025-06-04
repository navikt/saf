package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.tilgangskontroll.SafRequestContext;

import static no.nav.saf.tilgangskontroll.pep.PepAnswer.permit;

public abstract class StandardMsGraphBackedPep<T> extends Pep<T> {

	/**
	 * Sjekk NavIdent mot gruppemedlemskap i AD
	 * Bestemmer om kall skal få tilgang til ressurs.
	 * Implementerer:
	 * https://confluence.adeo.no/display/BOA/saf+-+Tilgangskontroll#safTilgangskontroll-TilgangsreglerforNAV-ansatte
	 * https://confluence.adeo.no/display/BOA/saf+-+Tilgangskontroll#safTilgangskontroll-Tilgangsreglerforservicebruker
	 *
	 * @param ressurs           Ressursen som skal sjekkes
	 * @param safRequestContext Kontekst for kallet
	 * @return Beslutning om tilgang fra intern ABAC PDP
	 */
	abstract PepAnswer verifyNavIdentGroupMembershipAccess(T ressurs, SafRequestContext safRequestContext);

	public PepAnswer hasAccessWithAnswer(T ressurs, SafRequestContext safRequestContext) {
		if (safRequestContext.getSecurityContext().isJwtAzureClientCredentialFlow()) {
			return verifyAzureClientCredentialFlowAccess(ressurs, safRequestContext);
		} else if (safRequestContext.isSystem()) {
			return verifyRestSTSCredentialFlowAccess(ressurs, safRequestContext);
		} else {
			return verifyNavIdentGroupMembershipAccess(ressurs, safRequestContext);
		}
	}

	PepAnswer verifyAzureClientCredentialFlowAccess(T ressurs, SafRequestContext safRequestContext) {
		return verifyAccessForSystemUser(ressurs, safRequestContext);
	}

	PepAnswer verifyRestSTSCredentialFlowAccess(T ressurs, SafRequestContext safRequestContext) {
		return verifyAccessForSystemUser(ressurs, safRequestContext);
	}

	protected PepAnswer verifyAccessForSystemUser(T ressurs, SafRequestContext safRequestContext) {
		return permit();
	}

}
