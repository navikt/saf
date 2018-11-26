package no.nav.saf.tjeneste.visningsmodell.kode;

import java.util.Arrays;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public enum JournalStatus {
	MOTTATT,
	JOURNALFOERT,
	FERDIGSTILT,
	EKSPEDERT,
	UNDER_ARBEID,
	FEILREGISTRERT,
	UTGAAR,
	AVBRUTT,
	UKJENT_BRUKER,
	RESERVERT,
	OPPLASTING_DOKUMENT;

	public static List<JournalStatus> mestBrukte() {
		return Arrays.asList(JOURNALFOERT, FERDIGSTILT, EKSPEDERT);
	}

	public static List<JournalStatus> asList() {
		return Arrays.asList(values());
	}
}
