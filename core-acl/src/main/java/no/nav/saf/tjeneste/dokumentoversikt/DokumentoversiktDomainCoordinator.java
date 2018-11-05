package no.nav.saf.tjeneste.dokumentoversikt;

import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.visningsmodell.DokumentInfo;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface DokumentoversiktDomainCoordinator {
	List<Journalpost> findJournalposter(DokumentoversiktArguments dokumentoversiktArguments, SafRequestContext safRequestContext);

	List<DokumentInfo> findDokumenter(Journalpost journalpost, SafRequestContext safRequestContext);
}
