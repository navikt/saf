package no.nav.saf.anticorruptionlayer.joark.domain.kode;

import static no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode.FS22;

public enum Sakstype {

	FAGSAK,
	GENERELL_SAK;

	public static Sakstype fromFagsaksystem(String fagsaksystem) {
		return fagsaksystem == null || FS22.name().equals(fagsaksystem) ? GENERELL_SAK : FAGSAK;
	}

}
