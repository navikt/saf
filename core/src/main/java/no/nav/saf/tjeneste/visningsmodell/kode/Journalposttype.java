package no.nav.saf.tjeneste.visningsmodell.kode;

import java.util.Arrays;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public enum Journalposttype {
	I,
	U,
	N;

	protected static final List<Journalposttype> ALL = asList();

	public static List<Journalposttype> asList() {
		return Arrays.asList(values());
	}
}
