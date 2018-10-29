package no.nav.saf.anticorruptionlayer.joark;

import no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.DokumentInfo;
import no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.Journalpost;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface JoarkAntiCorruptionLayer {
	List<Journalpost> hentJournalpostListeByArkivsaksnummer(String arkivsaksnummer);

	List<DokumentInfo> hentDokumentInfoListeByJournalpostIdAndArkivsak(String journalpostId, String arkivsaksnummer);
}
