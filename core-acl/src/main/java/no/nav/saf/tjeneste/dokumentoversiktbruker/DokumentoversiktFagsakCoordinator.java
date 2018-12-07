package no.nav.saf.tjeneste.dokumentoversiktbruker;

import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.visningsmodell.DokumentInfo;
import no.nav.saf.tjeneste.visningsmodell.Dokumentoversikt;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface DokumentoversiktFagsakCoordinator {
	Dokumentoversikt hentDokumentoversikt(DokumentoversiktFagsakArguments dokumentoversiktFagsakArguments, SafRequestContext safRequestContext);

	List<Journalpost> findJournalposter(DokumentoversiktFagsakArguments dokumentoversiktFagsakArguments, SafRequestContext safRequestContext);

	List<DokumentInfo> findDokumenter(Journalpost journalpost, SafRequestContext safRequestContext);
}
