package no.nav.saf.anticorruptionlayer.joark;

import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import no.nav.saf.tjeneste.visningsmodell.Sak;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface JoarkAntiCorruptionLayer {
	List<Journalpost> hentJournalpostListeByArkivsaksnummer(String arkivsaksnummer);

	List<Journalpost> hentJournalpostListeByArkivsaker(List<Sak> arkivsaksnummer);
}
