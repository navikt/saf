package no.nav.saf.tilgangskontroll.pep;

import lombok.Builder;
import lombok.Value;

import static no.nav.saf.tilgangskontroll.pep.AbacAnswer.AbacDecision.DENY;
import static no.nav.saf.tilgangskontroll.pep.AbacAnswer.AbacDecision.PERMIT;

/**
 * Inneholder beslutningen til PEP.
 *
 * @author Joakim Bjørnstad, Jbit AS
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

	public static AbacAnswer deny(final AbacDenyReason abacDenyReason) {
		return new AbacAnswer(DENY, abacDenyReason.toString());
	}

	public static AbacAnswer deny(final String denyReason) {
		return new AbacAnswer(DENY, denyReason);
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

		@Override
		public String toString() {
			return "cause=" + cause + ", deny_policy=" + policy + ", deny_rule=" + rule;
		}
	}
}
