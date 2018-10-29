package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.tilgangskontroll.SafRequestContext;

/**
 * Policy Enforcement Point for ABAC.
 *
 * Evaluerer tilgang til en ressurs T.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface PepEvaluator<T> {
	//TODO vi behøver en pålitelig requestcontext for bruker og annen sikkerhet her i tillegg! Det er ikke lov å putte dette i MDC!
	boolean hasAccess(T ressurs, SafRequestContext safRequestContext);
}
