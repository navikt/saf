package no.nav.saf.tjeneste.dokumentoversiktbruker;

import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.visningsmodell.DokumentInfo;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface DokumentoversiktCoordinator {
	List<DokumentInfo> findDokumenter(Journalpost journalpost, SafRequestContext safRequestContext);
}
