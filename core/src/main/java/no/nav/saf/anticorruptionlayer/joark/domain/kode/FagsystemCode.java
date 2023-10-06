package no.nav.saf.anticorruptionlayer.joark.domain.kode;

import no.nav.saf.domain.kode.Arkivsakssystem;

public enum FagsystemCode {
	PEN(Arkivsakssystem.PSAK),
	FS22(Arkivsakssystem.GSAK);

	FagsystemCode(Arkivsakssystem safArkivsaksystem) {
		this.safArkivsaksystem = safArkivsaksystem;
	}

	private final Arkivsakssystem safArkivsaksystem;

	public static Arkivsakssystem toSafArkivsaksystem(FagsystemCode joarkFagsystemCode) {
		if (joarkFagsystemCode == null) {
			//ingen tilhørende saf-kodeverdi
			return null;
		} else {
			return joarkFagsystemCode.safArkivsaksystem;
		}
	}
}
