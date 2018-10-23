package no.nav.saf.domain;

import no.nav.saf.domain.tilgangsmodell.TilgangBruker;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface TilgangsmodellRepository {
	TilgangBruker findTilgangBrukerByAktoerId(String aktoerId);
}
