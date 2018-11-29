package no.nav.saf.tjeneste.dokumentoversiktbruker;

import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import no.nav.saf.tjeneste.visningsmodell.kode.Tema;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface DokumentoversiktBrukerVisningsmodellRepository {
	List<Journalpost> findJournalposter(SafRequestContext safRequestContext,
										List<Tema> tema,
										String aktoerId,
										String foedselsnummer,
										List<String> journalpostIds);
}
