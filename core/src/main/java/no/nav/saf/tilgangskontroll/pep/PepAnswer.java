package no.nav.saf.tilgangskontroll.pep;

import lombok.Value;
import no.nav.saf.tilgangskontroll.pep.reasons.AbacDenyReason;

import static no.nav.saf.tilgangskontroll.pep.PepAnswer.PepDecision.DENY;
import static no.nav.saf.tilgangskontroll.pep.PepAnswer.PepDecision.PERMIT;

/**
 * Inneholder beslutningen til PEP.
 */
@Value
public class PepAnswer {
	PepDecision decision;
	/**
	 * Brukes til sporingslogging for ArcSight.
	 * https://confluence.adeo.no/display/BOA/saf+-+Sporingslogg+hentdokument
	 */
	String denyReasonSporing;
	AbacDenyReason pepDenyReason;

	public boolean isDeny() {
		return DENY == decision;
	}

	public boolean isPermit() {
		return PERMIT == decision;
	}

	public static PepAnswer permit() {
		return new PepAnswer(PERMIT, null, null);
	}

	public static PepAnswer deny(AbacDenyReason pepDenyReason) {
		return new PepAnswer(DENY, pepDenyReason.toString(), pepDenyReason);
	}

	public enum PepDecision {
		PERMIT,
		DENY
	}

}
