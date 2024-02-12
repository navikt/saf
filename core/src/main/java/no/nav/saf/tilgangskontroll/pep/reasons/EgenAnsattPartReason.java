package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.AbacAnswer;

import java.util.Map;

public final class EgenAnsattPartReason extends AbacDenyReason {
	public EgenAnsattPartReason(Map<String,String> advices) {
		super(advices, AbacAnswer.AbacDenyReasonCode.EGEN_ANSATT_PART);
	}
}