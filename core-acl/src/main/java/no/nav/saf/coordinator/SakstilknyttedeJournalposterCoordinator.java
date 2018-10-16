package no.nav.saf.coordinator;

import no.nav.saf.context.saf.domain.DokumentInfo;
import no.nav.saf.context.saf.domain.Journalpost;
import no.nav.saf.context.saf.domain.Sak;
import no.nav.saf.context.saf.domain.Tema;
import no.nav.saf.context.saf.domain.kode.Temakode;

import java.util.List;
import java.util.Set;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface SakstilknyttedeJournalposterCoordinator {

	Set<Tema> findTemaKnyttetTilAktoerIdAndFilterByTemakoder(String aktoerId, List<Temakode> temakoder);

	List<Sak> findSakerByAktoerIdAndTema(String aktoerId, Temakode tema);

	List<Journalpost> findJournalposterByArkivsak(String arkivsaksnummer);

	List<DokumentInfo> findDokumentInfoByJournalpostIdAndArkivsak(String journalpostId, String arkivsaksnummer);
}
