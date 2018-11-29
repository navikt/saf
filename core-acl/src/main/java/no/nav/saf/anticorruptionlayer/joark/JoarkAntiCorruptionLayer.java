package no.nav.saf.anticorruptionlayer.joark;

import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.JournalpostDto;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tjeneste.hentdokument.HentDokument;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalposttype;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalstatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface JoarkAntiCorruptionLayer {

	Map<String, JournalpostDto> hentJournalpostBulk(TilgangBruker tilgangBruker,
													List<TilgangSak> tilgangSakList,
													LocalDate fraDato,
													List<Journalposttype> inkluderJournalposttyper,
													List<Journalstatus> inkluderJournalstatuses);

	TilgangJournalpost hentTilgangJournalpost(String journalpostId, String dokumentId, String variantFormat);

	TilgangSak hentTilgangSak(String journalpostId, String dokumentId, String variantFormat);

	TilgangBruker hentTilgangBruker(String journalpostId, String dokumentId, String variantFormat);

	HentDokument hentDokument(String dokumentId, String variantFormat);

}
