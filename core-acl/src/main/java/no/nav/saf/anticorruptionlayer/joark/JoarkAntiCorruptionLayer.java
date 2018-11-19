package no.nav.saf.anticorruptionlayer.joark;

import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tjeneste.hentdokument.HentDokument;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import no.nav.saf.tjeneste.visningsmodell.Sak;
import no.nav.saf.tjeneste.visningsmodell.kode.JournalStatus;
import no.nav.saf.tjeneste.visningsmodell.kode.JournalpostType;
import no.nav.saf.tjeneste.visningsmodell.kode.Temakode;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface JoarkAntiCorruptionLayer {

	List<Journalpost> hentJournalpostListeByArkivsaker(List<Sak> saker);

	List<TilgangJournalpost> hentTilgangJournalpostListByArkivsaker(List<TilgangSak> tilgangSakList);

	List<TilgangJournalpost> hentTilgangJournalpostListByArkivsaker(TilgangBruker tilgangBruker,
																	List<TilgangSak> tilgangSakList,
																	LocalDate fraDato,
																	Collection<Temakode> inkluderTema,
																	List<JournalpostType> inkluderJournalposttyper,
																	List<JournalStatus> inkluderJournalstatus);

	List<Journalpost> hentVisningJournalposter(Map<String, Sak> sakMap, List<String> journalpostIds);

	TilgangJournalpost hentTilgangJournalpost(String journalpostId, String dokumentId, String variantFormat);

	TilgangSak hentTilgangSak(String journalpostId, String dokumentId, String variantFormat);

	TilgangBruker hentTilgangBruker(String journalpostId, String dokumentId, String variantFormat);

	HentDokument hentDokument(String dokumentId, String variantFormat);

}
