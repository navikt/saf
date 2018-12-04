package no.nav.saf.domain;

import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.argumenter.BrukerIdInput;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalposttype;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalstatus;
import no.nav.saf.tjeneste.visningsmodell.kode.Tema;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface TilgangsmodellRepository {
	TilgangBruker findTilgangBruker(BrukerIdInput brukerIdInput);

	List<TilgangSak> findTilgangSaker(TilgangBruker tilgangBruker, List<Tema> tema, SafRequestContext safRequestContext);

	List<TilgangSak> findTilgangSakListByTilgangBruker(TilgangBruker tilgangBruker, List<Tema> tema);

	List<TilgangJournalpost> findTilgangJournalposter(TilgangBruker tilgangBruker,
													  List<TilgangSak> tilgangSakList,
													  LocalDate fraDato,
													  List<Tema> inkluderTema, List<Journalposttype> inkluderJournalposttyper,
													  List<Journalstatus> inkluderJournalstatuses,
													  SafRequestContext safRequestContext);

	TilgangJournalpost findTilgangJournalpost(String journalpostId, String dokumentId, String variantFormat);

	TilgangDokumentInfo findTilgangDokumentInfo(String journalpostId, String dokumentId, String variantFormat);

	TilgangSak findTilgangSak(String journalpostId, String dokumentId, String variantFormat);

	TilgangBruker findTilgangBruker(String journalpostId, String dokumentId, String variantFormat);

	TilgangBruker findTilgangBrukerBySakId(String sakId, String arkivsaksystem);

	TilgangSak findTilgangSakBySakId(String sakId, String arkivsaksystem);
}
