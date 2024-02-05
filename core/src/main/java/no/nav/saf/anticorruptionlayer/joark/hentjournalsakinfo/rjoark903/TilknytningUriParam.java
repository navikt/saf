package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark903;

import no.nav.saf.domain.kode.Tilknytning;

public enum TilknytningUriParam {
	GJENBRUK,
	SPLITT;

	public static TilknytningUriParam toUriParam(Tilknytning tilknytning) {
		if (tilknytning == Tilknytning.GJENBRUK) {
			return GJENBRUK;
		}
		return GJENBRUK;
	}
}
