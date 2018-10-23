package no.nav.saf.legacycontext.joark;

import no.nav.saf.domain.visningsmodell.DokumentInfo;
import no.nav.saf.domain.visningsmodell.Journalpost;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface JoarkAcl {
	List<Journalpost> hentJournalpostListeByArkivsaksnummer(String arkivsaksnummer);

	List<DokumentInfo> hentDokumentInfoListeByJournalpostIdAndArkivsak(String journalpostId, String arkivsaksnummer);
}
