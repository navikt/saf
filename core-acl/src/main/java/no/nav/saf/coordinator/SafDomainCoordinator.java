package no.nav.saf.coordinator;

import no.nav.saf.domain.visningsmodell.Bruker;
import no.nav.saf.domain.visningsmodell.DokumentInfo;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.domain.visningsmodell.Sak;
import no.nav.saf.domain.visningsmodell.Tema;
import no.nav.saf.domain.visningsmodell.kode.Temakode;

import java.util.List;
import java.util.Set;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface SafDomainCoordinator {

	Bruker findBrukerByAktoerId(String aktoerId);

	Set<Tema> findTemaKnyttetTilAktoerIdAndFilterByTemakoder(String aktoerId, List<Temakode> temakoder);

	List<Sak> findSakerByAktoerIdAndTema(String aktoerId, Temakode tema);

	List<Journalpost> findJournalposterByArkivsak(String arkivsaksnummer);

	List<DokumentInfo> findDokumentInfoByJournalpostIdAndArkivsak(String journalpostId, String arkivsaksnummer);
}
