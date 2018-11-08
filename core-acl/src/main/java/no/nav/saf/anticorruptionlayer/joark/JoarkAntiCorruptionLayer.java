package no.nav.saf.anticorruptionlayer.joark;

import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import no.nav.saf.tjeneste.visningsmodell.Sak;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface JoarkAntiCorruptionLayer {

	List<Journalpost> hentJournalpostListeByArkivsaker(List<Sak> saker);

	List<TilgangJournalpost> hentTilgangJournalpostListByArkivsaker(List<TilgangSak> tilgangSakList);

}
