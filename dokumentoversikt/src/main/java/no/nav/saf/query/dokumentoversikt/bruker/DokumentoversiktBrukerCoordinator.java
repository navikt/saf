package no.nav.saf.query.dokumentoversikt.bruker;

import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.visningsmodell.Dokumentoversikt;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface DokumentoversiktBrukerCoordinator {
	Dokumentoversikt hentDokumentoversikt(DokumentoversiktBrukerArguments dokumentoversiktBrukerArguments, SafRequestContext safRequestContext);
}
