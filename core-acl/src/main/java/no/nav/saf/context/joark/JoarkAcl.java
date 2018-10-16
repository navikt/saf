package no.nav.saf.context.joark;

import no.nav.saf.context.saf.domain.DokumentInfo;
import no.nav.saf.context.saf.domain.Journalpost;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface JoarkAcl {
	List<Journalpost> hentJournalpostListeByArkivsaksnummer(String arkivsaksnummer);

	List<DokumentInfo> hentDokumentInfoListeByJournalpostIdAndArkivsak(String journalpostId, String arkivsaksnummer);
}
