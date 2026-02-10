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
	 * @return Beslutning om tilgang fra intern PDP
	 */
	abstract PepAnswer verifyNavIdentGroupMembershipAccess(T ressurs, SafRequestContext safRequestContext);

	@Override
	public PepAnswer hasAccessWithAnswer(T ressurs, SafRequestContext safRequestContext) {
		PepAnswer pepAnswer;

		if (safRequestContext.isSystem()) {
			pepAnswer = verifyAccessForSystem(ressurs, safRequestContext);
		} else {
			pepAnswer = verifyNavIdentGroupMembershipAccess(ressurs, safRequestContext);
		}

		if (pepAnswer.isDeny()) {
			logDeny(pepAnswer);
		}

		return pepAnswer;
	}
}
