package no.nav.saf.query.dokumentoversikt;

import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.tilgangskontroll.SafRequestContext;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface DokumentoversiktVisningsmodellRepository {
	List<Journalpost> findJournalposter(List<String> journalpostIds,
										SafRequestContext safRequestContext);
}
