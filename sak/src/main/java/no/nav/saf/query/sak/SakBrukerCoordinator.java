package no.nav.saf.query.sak;

import no.nav.saf.domain.visningsmodell.Sak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.argumenter.BrukerIdInput;

import java.util.List;

public interface SakBrukerCoordinator {
	public List<Sak> hentSaker(BrukerIdInput brukerIdInput, SafRequestContext safRequestContext);
}
