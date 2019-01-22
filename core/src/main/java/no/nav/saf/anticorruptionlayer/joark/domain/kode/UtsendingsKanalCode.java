package no.nav.saf.anticorruptionlayer.joark.domain.kode;

import no.nav.saf.domain.kode.Kanal;

public enum UtsendingsKanalCode {
	/**
	 * EESSI
	 */
	EESSI(Kanal.EESSI),
	/**
	 * ALTINN
	 */
	ALTINN(Kanal.ALTINN),
	/**
	 * Ditt NAV
	 */
	NAV_NO(Kanal.NAV_NO),
	/**
	 * Sentral print
	 */
	S(Kanal.SENTRAL_UTSKRIFT),
	/**
	 * Lokal print
	 */
	L(Kanal.LOKAL_UTSKRIFT),
	/**
	 * Sikker digital post
	 */
	SDP(Kanal.SDP),
	/**
	 * EIA
	 */
	EIA(Kanal.EIA),
	/**
	 * INGEN_DISTRIBUSJON
	 */
	INGEN_DISTRIBUSJON(Kanal.INGEN_DISTRIBUSJON);

	private final Kanal safKanal;

	UtsendingsKanalCode(Kanal safKanal) {
		this.safKanal = safKanal;
	}

	public Kanal getSafKanal() {
		return safKanal;
	}
}
