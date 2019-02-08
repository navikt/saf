package no.nav.saf.anticorruptionlayer.joark.domain.kode;

import no.nav.saf.domain.kode.Arkivsakssystem;

public enum FagsystemCode {

	/**
	 * Arena
	 */
	AO01(null),
	/**
	 * Infotrygd
	 */
	IT01(null),
	/**
	 * Bidrag
	 */
	BID(null),
	/**
	 * Pensjon
	 */
	PEN(Arkivsakssystem.PSAK),
	/**
	 * Øvrig
	 */
	OVR(null),
	/**
	 * Skanning
	 */
	MOT(null),
	/**
	 * Okonomi
	 */
	OKO(null),
	/**
	 * Bidrag innkreving
	 */
	BII(null),
	/**
	 * GOSYS
	 */
	FS22(Arkivsakssystem.GSAK),
	/**
	 * GSAK
	 */
	FS19(null),
	/**
	 * Utbetalingsmeldinger (UR)
	 */
	OB36(null);

	FagsystemCode(Arkivsakssystem safArkivsaksystem) {
		this.safArkivsaksystem = safArkivsaksystem;
	}

	private final Arkivsakssystem safArkivsaksystem;

	public static Arkivsakssystem toSafArkivsaksystem(FagsystemCode joarkFagsystemCode) {
		if (joarkFagsystemCode == null) {
			//ingen tilhørende saf-kodeverdi
			return null;
		} else {
			return Arkivsakssystem.valueOf(joarkFagsystemCode.name());
		}
	}
}
