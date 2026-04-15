package no.nav.saf.anticorruptionlayer.joark.domain.kode;

import no.nav.saf.domain.kode.Skjerming;

/**
 * Enum for codes in T_K_BEGRENSNING_TYPE.
 */
public enum SkjermingTypeCode {
	/**
	 * POL
	 */
	POL(Skjerming.POL),
	ARK(Skjerming.ARK),
	FEIL(Skjerming.FEIL);

	private final Skjerming safSkjerming;

	SkjermingTypeCode(Skjerming safSkjerming) {
		this.safSkjerming = safSkjerming;
	}

	public Skjerming getSafSkjerming() {
		return this.safSkjerming;
	}

	public static Skjerming toSafSkjerming(SkjermingTypeCode joarkSkjermingTypeCode) {
		return joarkSkjermingTypeCode == null ? null : joarkSkjermingTypeCode.getSafSkjerming();
	}
}
