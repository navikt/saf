package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.tilgangskontroll.SafRequestContext;

import static no.nav.saf.tilgangskontroll.pep.PepAnswer.permit;

public abstract class StandardTilgangsmaskinenBackedPep extends Pep<TilgangBruker> {

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
	abstract PepAnswer verifyNavIdentAccessToUser(TilgangBruker ressurs, SafRequestContext safRequestContext);

	public PepAnswer hasAccessWithAnswer(TilgangBruker ressurs, SafRequestContext safRequestContext) {
		if (safRequestContext.getSecurityContext().isJwtAzureClientCredentialFlow()) {
			return verifyAzureClientCredentialFlowAccess(ressurs, safRequestContext);
		} else if (safRequestContext.isSystem()) {
			return verifyRestSTSCredentialFlowAccess(ressurs, safRequestContext);
		} else {
			return verifyNavIdentAccessToUser(ressurs, safRequestContext);
		}
	}

	PepAnswer verifyAzureClientCredentialFlowAccess(TilgangBruker ressurs, SafRequestContext safRequestContext) {
		return verifyAccessForSystemUser(ressurs, safRequestContext);
	}

	PepAnswer verifyRestSTSCredentialFlowAccess(TilgangBruker ressurs, SafRequestContext safRequestContext) {
		return verifyAccessForSystemUser(ressurs, safRequestContext);
	}

	protected PepAnswer verifyAccessForSystemUser(TilgangBruker ressurs, SafRequestContext safRequestContext) {
		return permit();
	}

}
