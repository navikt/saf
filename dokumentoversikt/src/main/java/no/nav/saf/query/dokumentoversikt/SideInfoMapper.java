package no.nav.saf.query.dokumentoversikt;

import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.domain.visningsmodell.SideInfo;
import no.nav.saf.tilgangskontroll.SafRequestContext;

import java.util.Base64;
import java.util.List;

public class SideInfoMapper {
	public SideInfo mapFilteredSideInfo(String sluttJournalpostId, List<Journalpost> journalposter, SafRequestContext safRequestContext) {
		if (sluttJournalpostId == null || sluttJournalpostId.isEmpty()) {
			return SideInfo.empty();
		}
		JournalpostDto journalpostDto = safRequestContext.getRequestCache().getJournalpost(sluttJournalpostId);
		Long nextJournalpostId = journalpostDto.getNextJournalpostId();
		return new SideInfo(
				base64(sluttJournalpostId),
				finnesNesteJournalpostId(journalpostDto, nextJournalpostId),
				journalposter.size(),
				totaltAntall(sluttJournalpostId, safRequestContext)
		);
	}

	private String base64(String journalpostId) {
		if (journalpostId == null) {
			return null;
		}
		return Base64.getEncoder().encodeToString(journalpostId.getBytes());
	}

	private boolean finnesNesteJournalpostId(JournalpostDto journalpostDto, Long nextJournalpostId) {
		return nextJournalpostId != null && !journalpostDto.getJournalpostId().equals(nextJournalpostId);
	}

	private int totaltAntall(String sluttJournalpostId, SafRequestContext safRequestContext) {
		if (sluttJournalpostId == null) {
			return 0;
		}
		JournalpostDto journalpostDto = safRequestContext.getRequestCache().getJournalpost(sluttJournalpostId);
		return journalpostDto.getTotaltAntall().intValue();
	}
}
