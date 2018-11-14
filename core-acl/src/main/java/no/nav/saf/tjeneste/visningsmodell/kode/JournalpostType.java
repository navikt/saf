package no.nav.saf.tjeneste.visningsmodell.kode;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;

import java.util.Arrays;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public enum JournalpostType {
	I,
	U,
	N;

	@Deprecated // TODO fjern denne, skal ikke vite om joark i det hele tatt
	public static JournalpostType fromJoark(JournalpostTypeCode journalpostTypeCode) {
		if(journalpostTypeCode == null) {
			return null;
		}
		return JournalpostType.valueOf(journalpostTypeCode.name());
	}

	public static List<JournalpostType> asList() {
		return Arrays.asList(values());
	}
}
