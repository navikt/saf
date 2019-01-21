package no.nav.saf.domain;

import io.reactivex.Flowable;
import no.nav.saf.domain.kode.Journalposttype;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.argumenter.BrukerIdInput;
import no.nav.saf.tjeneste.argumenter.FagsakIdInput;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface TilgangsmodellRepository {
	TilgangBruker findTilgangBruker(BrukerIdInput brukerIdInput);

	List<TilgangBruker> findTilgangBrukerList(FagsakIdInput fagsakIdInput);

	List<TilgangSak> findTilgangSaker(List<TilgangBruker> tilgangBrukerList, FagsakIdInput fagsakIdInput, List<Tema> tema, SafRequestContext safRequestContext);

	Flowable<TilgangSak> findTilgangSaker(TilgangBruker tilgangBruker, List<Tema> tema, SafRequestContext safRequestContext);

	List<TilgangJournalpost> findTilgangJournalposter(List<TilgangBruker> tilgangBrukere,
													  List<TilgangSak> tilgangSakList,
													  LocalDate fraDato,
													  List<Tema> inkluderTema, List<Journalposttype> inkluderJournalposttyper,
													  List<Journalstatus> inkluderJournalstatuses,
													  Integer foerste, String etterPeker, Integer siste, String foerPeker,
													  SafRequestContext safRequestContext);

}
