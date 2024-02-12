package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.AbacAnswer;

import java.util.Map;

public final class StrengtFortroligAdresseReason extends AbacDenyReason {
	public StrengtFortroligAdresseReason(Map<String,String> advices) {
		super(advices, AbacAnswer.AbacDenyReasonCode.STRENGT_FORTROLIG_ADRESSE);
	}
}