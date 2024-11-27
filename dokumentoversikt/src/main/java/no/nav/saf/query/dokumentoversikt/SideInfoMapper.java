package no.nav.saf.query.dokumentoversikt;

import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.domain.visningsmodell.SideInfo;

import java.util.Base64;
import java.util.List;

public class SideInfoMapper {
	public static SideInfo mapFilteredSideInfo(JournalpostDto sisteJournalpostDto, List<Journalpost> journalposter) {
		if (sisteJournalpostDto == null) {
			return SideInfo.empty();
		}
		Long nextJournalpostId = sisteJournalpostDto.getNextJournalpostId();
		return new SideInfo(
				base64(sisteJournalpostDto.getJournalpostId()),
				finnesNesteJournalpostId(sisteJournalpostDto, nextJournalpostId),
				journalposter.size(),
				totaltAntall(sisteJournalpostDto)
		);
	}

	private static String base64(Long journalpostId) {
		return Base64.getEncoder().encodeToString(journalpostId.toString().getBytes());
	}

	private static boolean finnesNesteJournalpostId(JournalpostDto journalpostDto, Long nextJournalpostId) {
		return nextJournalpostId != null && !journalpostDto.getJournalpostId().equals(nextJournalpostId);
	}

	private static int totaltAntall(JournalpostDto journalpostDto) {
		return journalpostDto.getTotaltAntall().intValue();
	}
}
