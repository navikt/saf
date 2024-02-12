package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.AbacAnswer;

import java.util.Map;

public final class SkjermingReason extends AbacDenyReason {
	public SkjermingReason(String cause, String policy, String rule) {
		super(cause, policy, rule, AbacAnswer.AbacDenyReasonCode.SKJERMING);
	}

	public SkjermingReason(Map<String,String> advices) {
		super(advices, AbacAnswer.AbacDenyReasonCode.SKJERMING);
	}
}