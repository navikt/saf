package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.AbacAnswer;

import java.util.Map;

public final class GeografiReason extends AbacDenyReason {

	public GeografiReason(Map<String,String> advices) {
		super(advices, AbacAnswer.AbacDenyReasonCode.GEOGRAFI);
	}
}