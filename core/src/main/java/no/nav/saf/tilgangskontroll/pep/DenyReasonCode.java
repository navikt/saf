package no.nav.saf.tilgangskontroll.pep;

public enum DenyReasonCode {
	EGEN_ANSATT("deny_egen_ansatt", "skjermede_navansatte_og_familiemedlemmer", "behandle_skjermede_navansatte_og_familiemedlemmer_mangler_gruppetilgang"),
	EGEN_ANSATT_PART("deny_egen_ansatt_part"),
	HABILITET("deny_habilitet"),
	FORTROLIG_ADRESSE("deny_fortrolig_adresse", "adressebeskyttelse_fortrolig_adresse", "fortrolig_adresse_nok"),
	FORTROLIG_ADRESSE_PART("deny_fortrolig_adresse_part"),
	GEOGRAFI("deny_geografi", "fp4_geografi", "ingen_tilgang_enhet"),
	JOURNALSTATUS("deny_journalstatus"),
	ORGNR_NAV_STAT("deny_orgnr_nav_stat"),
	SKJERMING("deny_skjerming"),
	STRENGT_FORTROLIG_ADRESSE("deny_strengt_fortrolig_adresse", "adressebeskyttelse_strengt_fortrolig_adresse", "strengt_fortrolig_adresse_nok"),
	STRENGT_FORTROLIG_ADRESSE_UTLAND("deny_strengt_fortrolig_adresse_utland", "adressebeskyttelse_strengt_fortrolig_adresse_utland", "strengt_fortrolig_adresse_utland_nok"),
	STRENGT_FORTROLIG_ADRESSE_PART("deny_strengt_fortrolig_adresse_part"),
	STRENGT_FORTROLIG_ADRESSE_UTLAND_PART("deny_strengt_fortrolig_adresse_utland_part"),
	PERSON_UTLAND("deny_person_utland"),
	TEMA("deny_tema"),
	AVSLUTTET_SAK("deny_avsluttet_sak"),
	UKJENT("ukjent");

	private static final String ADVICE_UNDEFINED = "null:null";
	private final String advice;

	public final String code;

	DenyReasonCode(String code) {
		this.code = code;
		this.advice = ADVICE_UNDEFINED;
	}

	DenyReasonCode(String code, String denyPolicy, String denyRule) {
		this.code = code;
		this.advice = buildAdvice(denyPolicy, denyRule);
	}

	private static String buildAdvice(String denyPolicy, String denyRule) {
		if (denyPolicy == null && denyRule == null) {
			return ADVICE_UNDEFINED;
		}
		return denyPolicy + ":" + denyRule;
	}
}
