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

}
