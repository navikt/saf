package no.nav.saf.query.dokumentoversikt.journalstatus;

import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.tilgangskontroll.SafRequestContext;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface DokumentoversiktJournalstatusCoordinator {
	Dokumentoversikt hentDokumentoversikt(DokumentoversiktJournalstatusArguments dokumentoversiktJournalstatusArguments, SafRequestContext safRequestContext);
}
