package no.nav.saf.tilgangskontroll.pep;

import lombok.Builder;
import lombok.Value;

import static no.nav.saf.tilgangskontroll.pep.AbacAnswer.AbacDecision.DENY;
import static no.nav.saf.tilgangskontroll.pep.AbacAnswer.AbacDecision.PERMIT;

/**
 * Inneholder beslutningen til PEP.
 */
@Value
public class AbacAnswer {
	AbacDecision decision;
	/**
	 * Brukes til sporingslogging for ArcSight.
	 * https://confluence.adeo.no/display/BOA/saf+-+Sporingslogg+hentdokument
	 */
	String denyReasonSporing;

	public boolean isDeny() {
		return DENY == decision;
	}

	public boolean isPermit() {
		return PERMIT == decision;
	}

	public static AbacAnswer permit() {
		return new AbacAnswer(PERMIT, null);
	}

	public static AbacAnswer deny(AbacDenyReason abacDenyReason) {
		return new AbacAnswer(DENY, abacDenyReason.toString());
	}

	public static AbacAnswer deny(AbacDenyReasonCode abacDenyReasonCode) {
		return deny(new AbacDenyReason(null, null, null, abacDenyReasonCode));
	}

	public enum AbacDecision {
		PERMIT,
		DENY
	}

	@Builder
	@Value
	public static class AbacDenyReason {
		String cause;
		String policy;
		String rule;

		AbacDenyReasonCode abacDenyReasonCode;

		@Override
		public String toString() {
			return "cause=" + cause + ", deny_policy=" + policy + ", deny_rule=" + rule + ", reason_code=" + abacDenyReasonCode.code;
		}
	}
	/*

	public static final String attributeValueCause0001 = "cause-0001-manglerrolle";
	public static final String attributeValueCause0002 = "cause-0002-ikketilgangtilNAVbrukersenhet";
	public static final String attributeValueCause0005 = "cause-0005-feilstatus";
	public static final String attributeValueCause0006 = "cause-0006-kanikkefattevedtakiegensak";
	public static final String attributeValueCause0010 = "cause_0010_kontorsperre";
	public static final String attributeValueCause0012 = "cause_0012_eksternbruker_krever_innloggingsnivaa_4";
	public static final String attributeValueCause0013 = "cause_0013_ikketilgangtiltema";
	 */

	public enum AbacDenyReasonCode {
		EGEN_ANSATT("deny_egen_ansatt"),
		EGEN_ANSATT_PART("deny_egen_ansatt_part"),
		FORTROLIG_ADRESSE("deny_fortrolig_adresse"),
		FORTROLIG_ADRESSE_PART("deny_fortrolig_adresse_part"),
		GEOGRAFI("deny_geografi"),
		JOURNALSTATUS("deny_journalstatus"),
		ORGNR_NAV_STAT("deny_orgnr_nav_stat"),
		SKJERMING("deny_skjerming"),
		STRENGT_FORTROLIG_ADRESSE("deny_strengt_fortrolig_adresse"),
		STRENGT_FORTROLIG_ADRESSE_UTLAND("deny_strengt_fortrolig_adresse_utland"),
		STRENGT_FORTROLIG_ADRESSE_PART("deny_strengt_fortrolig_adresse_part"),
		STRENGT_FORTROLIG_ADRESSE_UTLAND_PART("deny_strengt_fortrolig_adresse_utland_part"),
		TEMA("deny_tema"),
		UKJENT("ukjent");

		public final String code;

		AbacDenyReasonCode(String code) {
			this.code = code;
		}
	}
}
