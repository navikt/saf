package no.nav.saf.tjeneste.dokumentoversiktbruker;

import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface DokumentoversiktBrukerVisningsmodellRepository {
	List<Journalpost> findJournalposter(List<String> journalpostIds,
										SafRequestContext safRequestContext);
}
