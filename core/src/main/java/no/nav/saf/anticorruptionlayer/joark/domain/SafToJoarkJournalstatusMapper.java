package no.nav.saf.anticorruptionlayer.joark.domain;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
import no.nav.saf.domain.kode.Journalstatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper fra saf sine journalstatuser til joark sine journalstatuser
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public class SafToJoarkJournalstatusMapper {
	public List<JournalStatusCode> map(List<Journalstatus> safJournalstatuser) {
		if (safJournalstatuser == null || safJournalstatuser.isEmpty()) {
			return new ArrayList<>();
		}
		final List<JournalStatusCode> joarkJournalStatusCodes = new ArrayList<>();
		safJournalstatuser.forEach(journalStatus -> {
			switch (journalStatus) {
				case MOTTATT:
					joarkJournalStatusCodes.add(JournalStatusCode.M);
					joarkJournalStatusCodes.add(JournalStatusCode.MO);
					break;
				case JOURNALFOERT:
					joarkJournalStatusCodes.add(JournalStatusCode.J);
					break;
				case FERDIGSTILT:
					joarkJournalStatusCodes.add(JournalStatusCode.FL);
					joarkJournalStatusCodes.add(JournalStatusCode.FS);
					break;
				case EKSPEDERT:
					joarkJournalStatusCodes.add(JournalStatusCode.E);
					break;
				case UNDER_ARBEID:
					joarkJournalStatusCodes.add(JournalStatusCode.D);
					break;
				case FEILREGISTRERT:
					// noop
					break;
				case UTGAAR:
					joarkJournalStatusCodes.add(JournalStatusCode.U);
					break;
				case AVBRUTT:
					joarkJournalStatusCodes.add(JournalStatusCode.A);
					break;
				case UKJENT_BRUKER:
					joarkJournalStatusCodes.add(JournalStatusCode.UB);
					break;
				case RESERVERT:
					joarkJournalStatusCodes.add(JournalStatusCode.R);
					break;
				case OPPLASTING_DOKUMENT:
					joarkJournalStatusCodes.add(JournalStatusCode.OD);
					break;
				default:
					// noop
					break;
			}
		});
		return joarkJournalStatusCodes;
	}
}
