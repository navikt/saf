package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.tilgangskontroll.SafRequestContext;

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

}
