package no.nav.saf.anticorruptionlayer.joark.domain.kode;

public enum Sakstype {

	FAGSAK,
	GENERELL_SAK;

	public static Sakstype fromFagsaksystem(String fagsaksystem){
		return fagsaksystem == null ||
				fagsaksystem.equalsIgnoreCase("FS22") ?
				GENERELL_SAK : FAGSAK;
	}

}
