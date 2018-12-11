package no.nav.saf.query.dokumentoversikt;

import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.JournalpostDto;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import no.nav.saf.tjeneste.visningsmodell.SideInfo;

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
		String startJournalpostId = startJournalpostId(journalposter);
		String sluttJournalpostId = sluttJournalpostId(journalposter);
		return new SideInfo(
				totaltAntall(startJournalpostId, safRequestContext),
				base64(sluttJournalpostId),
				finnesNesteSide(sluttJournalpostId, safRequestContext),
				base64(startJournalpostId),
				finnesForrigeSide(startJournalpostId, safRequestContext)
		);
	}

	private int totaltAntall(String journalpostId, SafRequestContext safRequestContext) {
		JournalpostDto journalpostDto = safRequestContext.getRequestCache().getObject(journalpostId);
		return journalpostDto.getTotaltAntall().intValue();
	}

	private String startJournalpostId(List<Journalpost> journalposter) {
		return journalposter.get(0).getJournalpostId();
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
		return journalpostDto.getNextJournalpostId() != null;
	}

	private boolean finnesForrigeSide(String startJournalpostId, SafRequestContext safRequestContext) {
		JournalpostDto journalpostDto = safRequestContext.getRequestCache().getObject(startJournalpostId);
		return journalpostDto.getPrevJournalpostId() != null;
	}
}
