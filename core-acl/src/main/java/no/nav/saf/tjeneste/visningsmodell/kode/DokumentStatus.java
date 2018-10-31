package no.nav.saf.tjeneste.visningsmodell.kode;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.DokumentStatusCode;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public enum DokumentStatus {
	/**
	 * Dokumentet er under redigering
	 */
	UNDER_REDIGERING,
	/**
	 * Dokumentet er ferdigstilt
	 */
	FERDIGSTILT,
	/**
	 * Dokumentet er avbrutt
	 */
	AVBRUTT;

	public static DokumentStatus fromJoark(DokumentStatusCode dokumentStatusCode) {
		if(dokumentStatusCode == null) {
			return null;
		}
		return DokumentStatus.valueOf(dokumentStatusCode.name());
	}
}
