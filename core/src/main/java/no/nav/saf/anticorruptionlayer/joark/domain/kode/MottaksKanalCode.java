package no.nav.saf.anticorruptionlayer.joark.domain.kode;

import no.nav.saf.tjeneste.visningsmodell.kode.Kanal;

public enum MottaksKanalCode {

	/**
	 * EESSI
	 */
	EESSI(Kanal.EESSI),
	/**
	 * EIA
	 */
	EIA(Kanal.EIA),
	/**
	 * nav.no
	 */
	NAV_NO(Kanal.NAV_NO),
	/**
	 * ALTINN
	 */
	ALTINN(Kanal.ALTINN),
	/**
	 * Skanning Pensjon
	 */
	SKAN_PEN(Kanal.SKAN_PEN),
	/**
	 * Skanning Nets
	 */
	SKAN_NETS(Kanal.SKAN_NETS),
	/**
	 * Eksternt oppslag
	 */
	EKST_OPPS(Kanal.EKST_OPPS);

	private final Kanal safKanal;

	MottaksKanalCode(Kanal safKanal) {
		this.safKanal = safKanal;
	}

	public Kanal getSafKanal() {
		return safKanal;
	}
}
