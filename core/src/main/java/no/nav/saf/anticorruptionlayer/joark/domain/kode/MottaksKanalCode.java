package no.nav.saf.anticorruptionlayer.joark.domain.kode;

import no.nav.saf.domain.kode.Kanal;

public enum MottaksKanalCode {
	EESSI(Kanal.EESSI),
	EIA(Kanal.EIA),
	NAV_NO(Kanal.NAV_NO),
	ALTINN(Kanal.ALTINN),
	SKAN_PEN(Kanal.SKAN_PEN),
	SKAN_NETS(Kanal.SKAN_NETS),
	EKST_OPPS(Kanal.EKST_OPPS),
	HELSENETTET(Kanal.HELSENETTET),
	NAV_NO_UINNLOGGET(Kanal.NAV_NO_UINNLOGGET);

	private final Kanal safKanal;

	MottaksKanalCode(Kanal safKanal) {
		this.safKanal = safKanal;
	}

	public Kanal getSafKanal() {
		return safKanal;
	}
}
