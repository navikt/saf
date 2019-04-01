package no.nav.saf.domain.kode;

import java.util.Arrays;
import java.util.List;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public enum Journalposttype {
	I,
	U,
	N;

	public static List<Journalposttype> asList() {
		return Arrays.asList(values());
	}
}
