package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.tilgangskontroll.SafRequestContext;

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
	 * @return Beslutning om tilgang fra intern PDP
	 */
	abstract PepAnswer verifyNavIdentAccessToUser(T ressurs, SafRequestContext safRequestContext);

	@Override
	public PepAnswer hasAccessWithAnswer(T ressurs, SafRequestContext safRequestContext) {
		PepAnswer pepAnswer;

		if (safRequestContext.isSystem()) {
			pepAnswer = verifyAccessForSystem(ressurs, safRequestContext);
		} else {
			pepAnswer = verifyNavIdentAccessToUser(ressurs, safRequestContext);
		}

		if (pepAnswer.isDeny()) {
			logDeny(pepAnswer);
		}

		return pepAnswer;
	}
}
