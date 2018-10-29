package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.tilgangskontroll.AbacLogger;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Implementerer PEP1:
 *
 * FP1 Behandling av Kode 6 Brukere
 * FP2 Behandling av Kode 7 Brukere
 * FP3 Egen ansatt
 * Saksbehandlers tilgang til enhet
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component("pep1")
public class Pep1EvaluatorImpl implements PepEvaluator<TilgangBruker> {

	// TODO inject ABAC integrasjon og send XACML-JSON request!

	private final AbacLogger abacLogger;

	@Inject
	public Pep1EvaluatorImpl(AbacLogger abacLogger) {
		this.abacLogger = abacLogger;
	}

	@Override
	public boolean hasAccess(TilgangBruker ressurs, SafRequestContext safRequestContext) {
		// TODO MMA-1119 - Implementer itest også! Bruk wiremock!
		return false;
	}
}
