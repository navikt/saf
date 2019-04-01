package no.nav.saf.query.dokumentoversikt;

import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.domain.visningsmodell.SideInfo;
import no.nav.saf.tilgangskontroll.SafRequestContext;

import java.util.Base64;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class SideInfoMapper {
	public SideInfo mapSideInfo(List<Journalpost> journalposter, SafRequestContext safRequestContext) {
		if(journalposter.isEmpty()) {
			return SideInfo.empty();
		}
		String sluttJournalpostId = sluttJournalpostId(journalposter);
		return new SideInfo(
				base64(sluttJournalpostId),
				finnesNesteSide(sluttJournalpostId, safRequestContext)
		);
	}

	private String sluttJournalpostId(List<Journalpost> journalposter) {
		return journalposter.get(journalposter.size() - 1).getJournalpostId();
	}

	private String base64(String journalpostId) {
		if(journalpostId == null) {
			return null;
		}
		return Base64.getEncoder().encodeToString(journalpostId.getBytes());
	}

	private boolean finnesNesteSide(String sluttJournalpostId, SafRequestContext safRequestContext) {
		JournalpostDto journalpostDto = safRequestContext.getRequestCache().getObject(sluttJournalpostId);
		return journalpostDto.getNextJournalpostId() != null && !journalpostDto.getJournalpostId().equals(journalpostDto.getNextJournalpostId());
	}
}
