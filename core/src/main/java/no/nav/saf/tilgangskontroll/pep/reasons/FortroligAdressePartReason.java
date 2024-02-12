package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.AbacAnswer;

import java.util.Map;

public final class FortroligAdressePartReason extends AbacDenyReason {

	public FortroligAdressePartReason(Map<String,String> advices) {
		super(advices, AbacAnswer.AbacDenyReasonCode.FORTROLIG_ADRESSE_PART);
	}
}