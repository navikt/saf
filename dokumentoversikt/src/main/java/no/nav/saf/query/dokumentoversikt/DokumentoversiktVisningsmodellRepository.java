package no.nav.saf.query.dokumentoversikt;

import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface DokumentoversiktVisningsmodellRepository {
	List<Journalpost> findJournalposter(List<String> journalpostIds,
										SafRequestContext safRequestContext);
}
