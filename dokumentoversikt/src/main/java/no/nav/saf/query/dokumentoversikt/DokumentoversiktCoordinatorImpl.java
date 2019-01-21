package no.nav.saf.query.dokumentoversikt;

import no.nav.saf.domain.visningsmodell.DokumentInfo;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.tilgangskontroll.SafRequestContext;
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
