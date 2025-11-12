package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.tilgangskontroll.SafRequestContext;

import static no.nav.saf.tilgangskontroll.pep.PepAnswer.permit;

public abstract class StandardTilgangsmaskinenBackedPep<T> extends Pep<T> {

	/**
	 * Sjekk NavIdent mot Tilgangsmaskinen for å se om de har tilgang til en gitt bruker
	 * Bestemmer om kall skal få tilgang til ressurs.
	 * Implementerer:
	 * https://confluence.adeo.no/display/BOA/saf+-+Tilgangskontroll#safTilgangskontroll-TilgangsreglerforNAV-ansatte
	 * https://confluence.adeo.no/display/BOA/saf+-+Tilgangskontroll#safTilgangskontroll-Tilgangsreglerforservicebruker
	 *
	 * @param ressurs           Ressursen som skal sjekkes - alltid en bruker
	 * @param safRequestContext Kontekst for kallet
	 * @return Beslutning om tilgang fra intern ABAC PDP
	 */
	abstract PepAnswer verifyNavIdentAccessToUser(T ressurs, SafRequestContext safRequestContext);

	public PepAnswer hasAccessWithAnswer(T ressurs, SafRequestContext safRequestContext) {
		if (safRequestContext.getSecurityContext().isJwtAzureClientCredentialFlow()) {
			return verifyAzureClientCredentialFlowAccess(ressurs, safRequestContext);
		} else if (safRequestContext.isSystem()) {
			return verifyRestSTSCredentialFlowAccess(ressurs, safRequestContext);
		} else {
			return verifyNavIdentAccessToUser(ressurs, safRequestContext);
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
