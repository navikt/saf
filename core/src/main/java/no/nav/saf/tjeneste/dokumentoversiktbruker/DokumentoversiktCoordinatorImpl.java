package no.nav.saf.tjeneste.dokumentoversiktbruker;

import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.visningsmodell.DokumentInfo;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
class DokumentoversiktCoordinatorImpl implements DokumentoversiktCoordinator {

	@Override
	public List<DokumentInfo> findDokumenter(Journalpost journalpost, SafRequestContext safRequestContext) {
		return journalpost.getDokumenter();
	}
}
