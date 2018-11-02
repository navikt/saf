package no.nav.saf.anticorruptionlayer.aktoerid;

import no.nav.saf.domain.tilgangsmodell.TilgangBruker;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface AktoerAntiCorruptionLayer {

	TilgangBruker hentTilgangBruker(String aktoerId);

}
