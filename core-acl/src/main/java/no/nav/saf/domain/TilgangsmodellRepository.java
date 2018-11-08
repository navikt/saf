package no.nav.saf.domain;

import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface TilgangsmodellRepository {

	TilgangBruker findTilgangBrukerByAktoerId(String aktoerId);

	List<TilgangSak> findTilgangSakListByTilgangBruker(TilgangBruker tilgangBruker);

	List<TilgangJournalpost> findTilgangJournalpostListByArkivsaker(List<TilgangSak> tilgangSakList);
}
