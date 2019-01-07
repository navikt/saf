package no.nav.saf.anticorruptionlayer.joark;

import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.JournalpostDto;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.HentDokument;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalposttype;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalstatus;
import no.nav.saf.tjeneste.visningsmodell.kode.Tema;

import java.time.LocalDate;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface JoarkAntiCorruptionLayer {

	List<JournalpostDto> finnJournalposter(List<String> identer,
										   List<TilgangSak> tilgangSakList,
										   LocalDate fraDato,
										   List<Tema> inkluderTema,
										   List<Journalposttype> inkluderJournalposttyper,
										   List<Journalstatus> inkluderJournalstatuses,
										   Integer foerste, String etterPeker, Integer siste, String foerPeker);

	TilgangJournalpost hentTilgangJournalpostFromSafRequestContext(SafRequestContext safRequestContext);

	TilgangSak hentTilgangSakFromSafRequestContext(SafRequestContext safRequestContext);

	TilgangBruker hentTilgangBruker(SafRequestContext safRequestContext);

	HentDokument hentDokument(String dokumentId, String variantFormat);

	Arkivsak hentArkivsakAndCacheJournalpostDto(String journalpostId, String dokumentId, String variantFormat, SafRequestContext safRequestContex);
}
