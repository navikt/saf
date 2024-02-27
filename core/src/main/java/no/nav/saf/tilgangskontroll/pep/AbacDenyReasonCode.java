package no.nav.saf.tilgangskontroll.pep;

import java.util.Map;

public enum AbacDenyReasonCode {
	EGEN_ANSATT("deny_egen_ansatt", "skjermede_navansatte_og_familiemedlemmer", "behandle_skjermede_navansatte_og_familiemedlemmer_mangler_gruppetilgang"),
	EGEN_ANSATT_PART("deny_egen_ansatt_part"),
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
	TEMA("deny_tema"),
	UKJENT("ukjent");

	private static final String ABAC_ADVICE_UNDEFINED = "null:null";
	private final String abacAdvice;

	public final String code;

	AbacDenyReasonCode(String code) {
		this.code = code;
		this.abacAdvice = ABAC_ADVICE_UNDEFINED;
	}

	AbacDenyReasonCode(String code, String abacDenyPolicy, String abacDenyRule) {
		this.code = code;
		this.abacAdvice = buildAbacAdvice(abacDenyPolicy, abacDenyRule);
	}

	public boolean matchesAbacAdvice(Map<String, String> advices) {
		if (ABAC_ADVICE_UNDEFINED.equalsIgnoreCase(abacAdvice)) {
			return false;
		}
		return abacAdvice.equalsIgnoreCase(buildAbacAdvice(advices.get("deny_policy"), advices.get("deny_rule")));
	}

	private static String buildAbacAdvice(String denyPolicy, String denyRule) {
		if (denyPolicy == null && denyRule == null) {
			return ABAC_ADVICE_UNDEFINED;
		}
		return denyPolicy + ":" + denyRule;
	}
}
