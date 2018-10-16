package no.nav.saf.context.saf.domain.kode;

import no.nav.saf.context.joark.domain.kode.DokumentStatusCode;

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
