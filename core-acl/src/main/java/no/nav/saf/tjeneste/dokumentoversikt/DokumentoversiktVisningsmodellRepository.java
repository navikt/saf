package no.nav.saf.tjeneste.dokumentoversikt;

import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import no.nav.saf.tjeneste.visningsmodell.kode.Temakode;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface DokumentoversiktVisningsmodellRepository {
	List<Journalpost> findJournalposterByAktoerId(String aktoerId, String foedselsnummer, List<Temakode> temaer);
	List<Journalpost> findJournalposter(String aktoerId, List<String> journalpostIds);
}
