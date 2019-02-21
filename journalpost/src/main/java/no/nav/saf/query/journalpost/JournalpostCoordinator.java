package no.nav.saf.query.journalpost;

import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.tilgangskontroll.SafRequestContext;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface JournalpostCoordinator {
	Journalpost hentJournalpost(String journalpostId, SafRequestContext safRequestContext);
}
