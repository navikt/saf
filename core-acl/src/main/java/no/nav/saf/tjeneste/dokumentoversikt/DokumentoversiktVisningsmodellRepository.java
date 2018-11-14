package no.nav.saf.tjeneste.dokumentoversikt;

import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import no.nav.saf.tjeneste.visningsmodell.kode.Temakode;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface DokumentoversiktVisningsmodellRepository {
	List<Journalpost> findJournalposterByAktoerId(String aktoerId, String foedselsnummer, List<Temakode> temaer);
	List<Journalpost> findJournalposter(TilgangBruker tilgangBruker, List<TilgangJournalpost> tilgangJournalposter);
}
