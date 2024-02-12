package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.AbacAnswer;

import java.util.Map;

public final class EgenAnsattReason extends AbacDenyReason {
	public EgenAnsattReason(Map<String,String> advices) {
		super(advices, AbacAnswer.AbacDenyReasonCode.EGEN_ANSATT);
	}
}