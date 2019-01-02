package no.nav.saf.anticorruptionlayer.joark.domain.kode;

import no.nav.saf.tjeneste.visningsmodell.kode.Journalposttype;

public enum JournalpostTypeCode {
	/**
	 * Inngående dokument
	 */
	I,
	/**
	 * Utgående dokument
	 */
	U,
	/**
	 * Internt notat
	 */
	N;

	public static Journalposttype mapToJournalpostType(JournalpostTypeCode journalpostTypeCode) {
		if(journalpostTypeCode == null) {
			return null;
		}
		return Journalposttype.valueOf(journalpostTypeCode.name());
	}

	public static JournalpostTypeCode fromJournalpostType(Journalposttype journalposttype) {
		if(journalposttype == null) {
			return null;
		}
		return JournalpostTypeCode.valueOf(journalposttype.name());
	}
}
