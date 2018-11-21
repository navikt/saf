package no.nav.saf.anticorruptionlayer.aktoer;

import no.nav.saf.domain.tilgangsmodell.TilgangBruker;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface AktoerAntiCorruptionLayer {
	TilgangBruker hentTilgangBrukerByAktoerId(String aktoerId);
	TilgangBruker hentTilgangBrukerByFoedselsnummer(String foedselsnummer);
}
