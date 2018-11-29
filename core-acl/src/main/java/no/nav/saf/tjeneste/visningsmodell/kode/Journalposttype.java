package no.nav.saf.tjeneste.visningsmodell.kode;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;

import java.util.Arrays;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public enum Journalposttype {
	I,
	U,
	N;

	@Deprecated // TODO fjern denne, skal ikke vite om joark i det hele tatt
	public static Journalposttype fromJoark(JournalpostTypeCode journalpostTypeCode) {
		if(journalpostTypeCode == null) {
			return null;
		}
		return Journalposttype.valueOf(journalpostTypeCode.name());
	}

	public static List<Journalposttype> asList() {
		return Arrays.asList(values());
	}
}
