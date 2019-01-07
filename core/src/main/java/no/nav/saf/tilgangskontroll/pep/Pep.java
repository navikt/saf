package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.tilgangskontroll.SafRequestContext;

/**
 * Policy Enforcement Point for ABAC.
 * <p>
 * Evaluerer tilgang til en ressurs T.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface Pep<T> {
	boolean hasAccess(T ressurs, SafRequestContext safRequestContext);
}
