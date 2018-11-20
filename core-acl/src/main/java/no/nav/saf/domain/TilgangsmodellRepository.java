package no.nav.saf.domain;

import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tjeneste.visningsmodell.kode.JournalStatus;
import no.nav.saf.tjeneste.visningsmodell.kode.JournalpostType;
import no.nav.saf.tjeneste.visningsmodell.kode.Temakode;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface TilgangsmodellRepository {

	TilgangBruker findTilgangBrukerByAktoerId(String aktoerId);

	List<TilgangSak> findTilgangSakListByTilgangBruker(TilgangBruker tilgangBruker);

	List<TilgangJournalpost> findTilgangJournalpostListByArkivsaker(List<TilgangSak> tilgangSakList);

	List<TilgangJournalpost> findTilgangJournalposter(TilgangBruker tilgangBruker,
													  List<TilgangSak> tilgangSakList,
													  LocalDate fraDato,
													  Collection<Temakode> inkluderTema,
													  List<JournalpostType> inkluderJournalposttyper,
													  List<JournalStatus> inkluderJournalstatus);

	TilgangJournalpost findTilgangJournalpost(String journalpostId, String dokumentId, String variantFormat);

	TilgangSak findTilgangSak(String journalpostId, String dokumentId, String variantFormat);

	TilgangBruker findTilgangBruker(String journalpostId, String dokumentId, String variantFormat);

	TilgangBruker findTilgangBrukerBySakId(String sakId);
}
