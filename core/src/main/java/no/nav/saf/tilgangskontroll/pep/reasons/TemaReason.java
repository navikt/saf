package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.AbacAnswer;

import java.util.Map;

public final class TemaReason extends AbacDenyReason {
	public TemaReason(String cause, String policy, String rule) {
		super(cause, policy, rule, AbacAnswer.AbacDenyReasonCode.TEMA);
	}

	public TemaReason(Map<String,String> advices) {
		super(advices, AbacAnswer.AbacDenyReasonCode.TEMA);
	}
}