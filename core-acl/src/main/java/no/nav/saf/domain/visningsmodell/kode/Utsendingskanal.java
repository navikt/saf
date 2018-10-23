package no.nav.saf.domain.visningsmodell.kode;

import no.nav.saf.legacycontext.joark.domain.kode.UtsendingsKanalCode;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public enum Utsendingskanal {
	/** PSELV */
	PSELV,
	/** EESSI */
	EESSI,
	/** ALTINN */
	ALTINN,
	/** Ditt NAV */
	NAV_NO,
	/** E-post */
	E_POST,
	/** Sentral print */
	S,
	/** Lokal print */
	L,
	/** Sikker digital post */
	SDP,
	/** EIA */
	EIA,
	/**
	 * INGEN_DISTRIBUSJON
	 */
	INGEN_DISTRIBUSJON;

	public static Utsendingskanal fromJoark(UtsendingsKanalCode utsendingsKanalCode) {
		if(utsendingsKanalCode == null) {
			return null;
		}
		return Utsendingskanal.valueOf(utsendingsKanalCode.name());
	}
}
