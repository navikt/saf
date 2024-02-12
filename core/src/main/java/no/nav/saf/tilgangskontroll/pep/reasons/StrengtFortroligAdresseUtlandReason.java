package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.AbacAnswer;

import java.util.Map;

public final class StrengtFortroligAdresseUtlandReason extends AbacDenyReason {
	public StrengtFortroligAdresseUtlandReason(Map<String,String> advices) {
		super(advices, AbacAnswer.AbacDenyReasonCode.STRENGT_FORTROLIG_ADRESSE_UTLAND);
	}
}