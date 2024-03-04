package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode;

import java.util.Map;

public final class StrengtFortroligAdresseUtlandReason extends AbacDenyReason {
	public StrengtFortroligAdresseUtlandReason(Map<String, String> advices) {
		super(advices, AbacDenyReasonCode.STRENGT_FORTROLIG_ADRESSE_UTLAND);
	}

	public String getHumanReadableDenyReason() {
		return "Du har ikke tilgang til brukeren fordi han / hun har strengt fortrolig adresse utland." + VIKAFOSSEN;
	}
}