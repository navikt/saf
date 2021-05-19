package no.nav.saf.anticorruptionlayer.aktoer;

import no.nav.saf.domain.tilgangsmodell.TilgangBruker;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface PdlAntiCorruptionLayer {
	TilgangBruker hentTilgangBrukerByAktoerId(String aktoerId);
	TilgangBruker hentTilgangBrukerByFoedselsnummer(String foedselsnummer);
	List<TilgangBruker> hentTilgangBrukerListByAktoerIdList(List<String> aktoerIdList);
}
