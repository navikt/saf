package no.nav.saf.query.journalpost;

import no.nav.saf.domain.visningsmodell.Journalpost;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface JournalpostCoordinator {
	Journalpost hentJournalpost(String journalpostId);
}
