package no.nav.saf.query.dokumentoversikt.fagsak;

import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.visningsmodell.Dokumentoversikt;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface DokumentoversiktFagsakCoordinator {
	Dokumentoversikt hentDokumentoversikt(DokumentoversiktFagsakArguments dokumentoversiktFagsakArguments, SafRequestContext safRequestContext);
}
