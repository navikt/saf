package no.nav.saf.query.dokumentoversikt;

import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.domain.visningsmodell.SideInfo;
import no.nav.saf.query.dokumentoversikt.arguments.DokumentoversiktPagination;
import no.nav.saf.tilgangskontroll.SafRequestContext;

import java.util.Base64;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class SideInfoMapper {
	public SideInfo mapSideInfo(DokumentoversiktPagination.SeekPagination pagination, List<Journalpost> journalposter, SafRequestContext safRequestContext) {
		if (journalposter.isEmpty()) {
			return SideInfo.empty();
		}
		String sluttJournalpostId = sluttJournalpostId(journalposter);
		return new SideInfo(
				base64(sluttJournalpostId),
				finnesNesteSide(pagination.getFoerste(), journalposter, sluttJournalpostId, safRequestContext),
				journalposter.size(),
				totaltAntall(sluttJournalpostId, safRequestContext)
		);
	}

	public SideInfo mapFilteredSideInfo(String sluttJournalpostId, List<Journalpost> journalposter, SafRequestContext safRequestContext) {
		if (sluttJournalpostId == null || sluttJournalpostId.isEmpty()) {
			return SideInfo.empty();
		}
		JournalpostDto journalpostDto = safRequestContext.getRequestCache().getObject(sluttJournalpostId);
		Long nextJournalpostId = journalpostDto.getNextJournalpostId();
		return new SideInfo(
				base64(sluttJournalpostId),
				finnesNesteJournalpostId(journalpostDto, nextJournalpostId),
				journalposter.size(),
				totaltAntall(sluttJournalpostId, safRequestContext)
		);
	}

	private String sluttJournalpostId(List<Journalpost> journalposter) {
		return journalposter.get(journalposter.size() - 1).getJournalpostId();
	}

	private String base64(String journalpostId) {
		if (journalpostId == null) {
			return null;
		}
		return Base64.getEncoder().encodeToString(journalpostId.getBytes());
	}

	private boolean finnesNesteSide(int foerste, List<Journalpost> journalposter, String sluttJournalpostId, SafRequestContext safRequestContext) {
		if (journalposter.size() < foerste) {
			return false;
		} else {
			JournalpostDto journalpostDto = safRequestContext.getRequestCache().getObject(sluttJournalpostId);
			Long nextJournalpostId = journalpostDto.getNextJournalpostId();
			return finnesNesteJournalpostId(journalpostDto, nextJournalpostId);
		}
	}

	private boolean finnesNesteJournalpostId(JournalpostDto journalpostDto, Long nextJournalpostId) {
		return nextJournalpostId != null && !journalpostDto.getJournalpostId().equals(nextJournalpostId);
	}

	private int totaltAntall(String sluttJournalpostId, SafRequestContext safRequestContext) {
		if(sluttJournalpostId == null) {
			return 0;
		}
		JournalpostDto journalpostDto = safRequestContext.getRequestCache().getObject(sluttJournalpostId);
		return journalpostDto.getTotaltAntall().intValue();
	}
}
