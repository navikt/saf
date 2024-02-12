package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.AbacAnswer;

import java.util.Map;

public final class OrgnrNavStatReaason extends AbacDenyReason {
	public OrgnrNavStatReaason(Map<String,String> advices) {
		super(advices, AbacAnswer.AbacDenyReasonCode.ORGNR_NAV_STAT);
	}
}