package no.nav.saf.anticorruptionlayer.joark.domain.kode;

import no.nav.saf.domain.kode.Skjerming;

/**
 * Enum for codes in T_K_BEGRENSNING_TYPE.
 *
 * @author Ketill Fenne, Visma Consulting
 */
public enum SkjermingTypeCode {
	/**
	 * POL
	 */
	POL(Skjerming.POL);

	private final Skjerming safSkjerming;

	SkjermingTypeCode(Skjerming safSkjerming) {
		this.safSkjerming = safSkjerming;
	}

	public Skjerming getSafSkjerming() {
		return this.safSkjerming;
	}

	public static Skjerming toSafSkjerming(SkjermingTypeCode joarkSkjermingTypeCode) {
		try {
			return joarkSkjermingTypeCode.getSafSkjerming();
		} catch (NullPointerException e) {
			return null;
		}
	}
}
