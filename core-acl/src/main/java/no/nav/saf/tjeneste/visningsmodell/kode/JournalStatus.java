package no.nav.saf.tjeneste.visningsmodell.kode;

import java.util.Arrays;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public enum JournalStatus {

	MIDLERTIDIG,
	ENDELIG,
	FERDIGSTILT,
	EKSPEDERT,
	UNDER_ARBEID,
	FEILREGISTRERT,
	UTGAAR,
	AVBRUTT,
	UKJENT_BRUKER;

	public static List<JournalStatus> asList() {
		return Arrays.asList(values());
	}
}
