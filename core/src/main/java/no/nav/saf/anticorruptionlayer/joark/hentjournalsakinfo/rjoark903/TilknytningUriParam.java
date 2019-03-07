package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark903;

import no.nav.saf.domain.kode.Tilknytning;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public enum TilknytningUriParam {
	GJENBRUK,
	SPLITT;

	public static TilknytningUriParam toUriParam(Tilknytning tilknytning) {
		switch (tilknytning) {
			case GJENBRUK:
				return GJENBRUK;
			default:
				return GJENBRUK;
		}
	}
}
