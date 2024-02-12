package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.AbacAnswer;

import java.util.Map;

public final class FortroligAdresseReason extends AbacDenyReason {
	public FortroligAdresseReason(Map<String,String> advices) {
		super(advices, AbacAnswer.AbacDenyReasonCode.FORTROLIG_ADRESSE);
	}
}