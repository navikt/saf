package no.nav.saf.tilgangskontroll.pep;

import lombok.Value;
import no.nav.saf.tilgangskontroll.pep.reasons.AbacDenyReason;

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
	AbacDenyReason abacDenyReason;

	public boolean isDeny() {
		return DENY == decision;
	}

	public boolean isPermit() {
		return PERMIT == decision;
	}

	public static AbacAnswer permit() {
		return new AbacAnswer(PERMIT, null, null);
	}

	public static AbacAnswer deny(AbacDenyReason abacDenyReason) {
		return new AbacAnswer(DENY, abacDenyReason.toString(), abacDenyReason);
	}

	public enum AbacDecision {
		PERMIT,
		DENY
	}

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
