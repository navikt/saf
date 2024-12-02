package no.nav.saf.anticorruptionlayer.joark.domain.kode;

import no.nav.saf.domain.kode.Kanal;

public enum UtsendingsKanalCode {
	EESSI(Kanal.EESSI),
	ALTINN(Kanal.ALTINN),
	NAV_NO(Kanal.NAV_NO),
	S(Kanal.SENTRAL_UTSKRIFT),
	L(Kanal.LOKAL_UTSKRIFT),
	SDP(Kanal.SDP),
	EIA(Kanal.EIA),
	INGEN_DISTRIBUSJON(Kanal.INGEN_DISTRIBUSJON),
	TRYGDERETTEN(Kanal.TRYGDERETTEN),
	HELSENETTET(Kanal.HELSENETTET),
	NAV_NO_CHAT(Kanal.NAV_NO_CHAT),
	DPVT(Kanal.DPVT),
	DPO(Kanal.DPO);

	private final Kanal safKanal;

	UtsendingsKanalCode(Kanal safKanal) {
		this.safKanal = safKanal;
	}

	public Kanal getSafKanal() {
		return safKanal;
	}
}
