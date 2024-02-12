package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.AbacAnswer;

public final class UkjentReason extends AbacDenyReason {
	public UkjentReason(String cause, String policy, String rule) {
		super(cause, policy, rule, AbacAnswer.AbacDenyReasonCode.UKJENT);
	}

	public UkjentReason() {
		this(null, null, null);
	}
}