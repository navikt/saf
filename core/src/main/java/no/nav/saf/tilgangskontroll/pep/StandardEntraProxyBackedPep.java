package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.tilgangskontroll.SafRequestContext;

public abstract class StandardEntraProxyBackedPep<T> extends Pep<T> {

	/**
	 * Sjekk NavIdent mot Entra-Proxy for å se om de har tilgang til et gitt tema
	 * Bestemmer om kall skal få tilgang til ressurs.
	 * Implementerer:
	 * https://confluence.adeo.no/display/BOA/saf+-+Tilgangskontroll#safTilgangskontroll-TilgangsreglerforNAV-ansatte
	 * https://confluence.adeo.no/display/BOA/saf+-+Tilgangskontroll#safTilgangskontroll-Tilgangsreglerforservicebruker
	 *
	 * @param ressurs           Ressursen som skal sjekkes - alltid en bruker
	 * @param safRequestContext Kontekst for kallet
	 * @return Beslutning om tilgang fra internt PDP
	 */
	abstract PepAnswer verifyNavIdentAccessToTema(T ressurs, SafRequestContext safRequestContext);

	@Override
	public PepAnswer hasAccessWithAnswer(T ressurs, SafRequestContext safRequestContext) {
		PepAnswer pepAnswer;

		if (safRequestContext.isSystem()) {
			pepAnswer = verifyAccessForSystem(ressurs, safRequestContext);
		} else {
			pepAnswer = verifyNavIdentAccessToTema(ressurs, safRequestContext);
		}

		if (pepAnswer.isDeny() && safRequestContext.isUserIdNavAnsatt()) {
			logDeny(pepAnswer);
		}

		return pepAnswer;
	}
}
