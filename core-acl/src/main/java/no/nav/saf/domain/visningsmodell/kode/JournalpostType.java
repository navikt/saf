package no.nav.saf.domain.visningsmodell.kode;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public enum JournalpostType {
	I,
	U,
	N;

	public static JournalpostType fromJoark(JournalpostTypeCode journalpostTypeCode) {
		if(journalpostTypeCode == null) {
			return null;
		}
		return JournalpostType.valueOf(journalpostTypeCode.name());
	}
}
