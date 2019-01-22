package no.nav.saf.query.dokumentoversikt.bruker;

import no.nav.saf.domain.visningsmodell.Dokumentoversikt;
import no.nav.saf.tilgangskontroll.SafRequestContext;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface DokumentoversiktBrukerCoordinator {
	Dokumentoversikt hentDokumentoversikt(DokumentoversiktBrukerArguments dokumentoversiktBrukerArguments, SafRequestContext safRequestContext);
}
