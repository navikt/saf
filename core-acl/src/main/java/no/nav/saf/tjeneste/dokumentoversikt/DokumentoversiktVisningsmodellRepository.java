package no.nav.saf.tjeneste.dokumentoversikt;

import no.nav.saf.tjeneste.visningsmodell.Journalpost;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface DokumentoversiktVisningsmodellRepository {
	List<Journalpost> findJournalposter(String aktoerId, String foedselsnummer, List<String> journalpostIds);
}
