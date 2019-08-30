package no.nav.saf.domain;

import no.nav.saf.domain.kode.Journalposttype;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface TilgangsmodellRepository {
	List<TilgangJournalpost> findTilgangJournalposter(List<TilgangBruker> tilgangBrukere,
													  List<TilgangSak> tilgangSakList,
													  LocalDate fraDato,
													  List<Journalposttype> inkluderJournalposttyper,
													  List<Journalstatus> inkluderJournalstatuses,
													  Integer foerste, String etterPeker, Integer siste, String foerPeker,
													  SafRequestContext safRequestContext);

	List<TilgangJournalpost> findTilgangJournalposterStatus(LocalDate fraDato,
															List<Journalposttype> inkluderJournalposttyper,
															Journalstatus journalstatus,
															Integer foerste, String etterPeker,
															SafRequestContext safRequestContext);

}
