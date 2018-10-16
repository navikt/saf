package no.nav.saf.context.saf.domain.kode;

import io.leangen.graphql.annotations.GraphQLEnumValue;
import no.nav.saf.context.joark.domain.kode.DokumentKategoriCode;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public enum Dokumentkategori {
	/**
	 * Brev
	 */
	B,
	/**
	 * Vedtaksbrev
	 */
	VB,
	/**
	 * Infobrev
	 */
	IB,
	/**
	 * Elektronisk skjema
	 */
	ES,
	/**
	 * Tolkbart skjema
	 */
	TS,
	/**
	 * Ikke tolkbart skjema
	 */
	IS,
	/**
	 * E-post
	 */
	EP,
	/**
	 * Faktura
	 */
	F,
	/**
	 * Konverterte data fra system
	 */
	KS,
	/**
	 * Konvertert fra elektronisk arkiv
	 */
	KD,
	/**
	 * Konvertert fra papirarkiv (skannet)
	 */
	KM,
	/**
	 * Strukturert elektronisk dokument
	 */
	SED,
	/**
	 * SystemSED
	 */
	SYS_SED,
	/**
	 * Publikumsblankett EØS
	 */
	PUBL_BLANKETT_EOS,
	/**
	 * E-blankett
	 */
	E_BLANKETT,
	/**
	 * Elektronisk dialog (brukerdialog)
	 */
	ELEKTRONISK_DIALOG,
	/**
	 * Referat (brukerdialog)
	 */
	REFERAT,

	/**
	 * Referat fra samtale med bruker
	 */
	FORVALTNINGSNOTAT,

	/**
	 * Søknad
	 */
	SOK,

	/**
	 * Klage eller anke
	 */
	KA;

	public static Dokumentkategori fromJoark(DokumentKategoriCode dokumentKategoriCode) {
		if(dokumentKategoriCode == null) {
			return null;
		}
		return Dokumentkategori.valueOf(dokumentKategoriCode.name());
	}
}
