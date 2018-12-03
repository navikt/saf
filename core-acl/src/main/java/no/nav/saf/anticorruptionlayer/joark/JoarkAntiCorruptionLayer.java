package no.nav.saf.anticorruptionlayer.joark;

import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.JournalpostDto;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tjeneste.hentdokument.HentDokument;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalposttype;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalstatus;
import no.nav.saf.tjeneste.visningsmodell.kode.Tema;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface JoarkAntiCorruptionLayer {

	List<JournalpostDto> hentJournalpostBulk(TilgangBruker tilgangBruker,
											 List<TilgangSak> tilgangSakList,
											 LocalDate fraDato,
											 List<Tema> inkluderTema,
											 List<Journalposttype> inkluderJournalposttyper,
											 List<Journalstatus> inkluderJournalstatuses);

	TilgangJournalpost hentTilgangJournalpost(String journalpostId, String dokumentId, String variantFormat);

	TilgangDokumentInfo hentTilgangDokumentInfo(String journalpostId, String dokumentId, String variantFormat);

	TilgangSak hentTilgangSak(String journalpostId, String dokumentId, String variantFormat);

	TilgangBruker hentTilgangBruker(String journalpostId, String dokumentId, String variantFormat);

	HentDokument hentDokument(String dokumentId, String variantFormat);

}
