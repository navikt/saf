package no.nav.saf.tjeneste.dokumentoversikt;

import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.visningsmodell.Bruker;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface DokumentoversiktDomainCoordinator {
	Bruker findBrukerByAktoerId(String aktoerId, SafRequestContext safRequestContext);
	List<Journalpost> findJournalposterByAktoerId(String aktoerId, SafRequestContext safRequestContext);
}
