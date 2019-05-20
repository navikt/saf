package no.nav.saf.anticorruptionlayer.joark.domain.kode;

/**
 * @author Olav Røstvold Thorsen, Visma Consulting.
 */
public enum AvsenderMottakerIdTypeCode {

	/**
	 * Fødselsnummer
	 */
	FNR,
	/**
	 * Organisasjonsnummer
	 */
	ORGNR,

	/**
	 * Tom AvsenderMottakerId
	 */
	NULL,

	/**
	 * Ukjent AvdenserMottakerId
	 */
	UKJENT;
}
