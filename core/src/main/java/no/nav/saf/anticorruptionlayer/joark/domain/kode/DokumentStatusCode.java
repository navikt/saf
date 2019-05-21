package no.nav.saf.anticorruptionlayer.joark.domain.kode;

import no.nav.saf.domain.kode.Dokumentstatus;

public enum DokumentStatusCode {
	/**
	 * Dokumentet er under redigering
	 */
	UNDER_REDIGERING(Dokumentstatus.UNDER_REDIGERING),
	/**
	 * Dokumentet er ferdigstilt
	 */
	FERDIGSTILT(Dokumentstatus.FERDIGSTILT),
	/**
	 * Dokumentet er avbrutt
	 */
	AVBRUTT(Dokumentstatus.AVBRUTT);

	private final Dokumentstatus safDokumentstatus;

	DokumentStatusCode(Dokumentstatus safDokumentstatus) {
		this.safDokumentstatus = safDokumentstatus;
	}

	public Dokumentstatus toSafDokumentstatus() {
		return safDokumentstatus;
	}
}
