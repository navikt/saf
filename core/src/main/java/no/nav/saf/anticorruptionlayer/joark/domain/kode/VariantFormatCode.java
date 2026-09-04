package no.nav.saf.anticorruptionlayer.joark.domain.kode;

import no.nav.saf.domain.kode.Variantformat;

public enum VariantFormatCode {
	/**
	 * Produksjonsformat
	 */
	PRODUKSJON(Variantformat.PRODUKSJON),
	/**
	 * Arkivformat
	 */
	ARKIV(Variantformat.ARKIV),
	/**
	 * SkanningMetadata
	 */
	SKANNING_META(null),
	/**
	 * BrevbestillingData
	 */
	BREVBESTILLING(null),
	/**
	 * Originalformat
	 */
	ORIGINAL(Variantformat.ORIGINAL),
	/**
	 * Sladdetformat
	 */
	SLADDET(Variantformat.SLADDET),
	/**
	 * Produksjonsformat DLF
	 */
	PRODUKSJON_DLF(Variantformat.PRODUKSJON_DLF),
	/**
	 * versjon med infotekster
	 */
	FULLVERSJON(Variantformat.FULLVERSJON);

	private final Variantformat safVariantformat;

	VariantFormatCode(Variantformat safVariantformat) {
		this.safVariantformat = safVariantformat;
	}

	public Variantformat getSafVariantformat() {
		return safVariantformat;
	}

	public static VariantFormatCode fromString(String variantFormat) {
		return variantFormat == null ? null : valueOf(variantFormat);
	}

	public static Variantformat toSafVariantformat(VariantFormatCode joarkVariantFormatCode) {
		return joarkVariantFormatCode == null ? null : joarkVariantFormatCode.getSafVariantformat();
	}

	public static Variantformat toSafVariantformat(String variantFormat) {
		try {
			return variantFormat == null ? null : VariantFormatCode.valueOf(variantFormat).getSafVariantformat();
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

}
