package no.nav.saf.domain;

import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.visningsmodell.Brukeridentifikator;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalposttype;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalstatus;
import no.nav.saf.tjeneste.visningsmodell.kode.Tema;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface TilgangsmodellRepository {

	TilgangBruker findTilgangBruker(Brukeridentifikator brukeridentifikator);

	List<TilgangSak> findTilgangSakListByTilgangBruker(TilgangBruker tilgangBruker, List<Tema> tema);

	List<TilgangJournalpost> findTilgangJournalposter(SafRequestContext safRequestContext,
													  TilgangBruker tilgangBruker,
													  List<TilgangSak> tilgangSakList,
													  LocalDate fraDato,
													  List<Journalposttype> inkluderJournalposttyper,
													  List<Journalstatus> inkluderJournalstatuses);

	TilgangJournalpost findTilgangJournalpost(String journalpostId, String dokumentId, String variantFormat);

	TilgangSak findTilgangSak(String journalpostId, String dokumentId, String variantFormat);

	TilgangBruker findTilgangBruker(String journalpostId, String dokumentId, String variantFormat);

	TilgangBruker findTilgangBrukerBySakId(String sakId, String arkivsaksystem);
}
