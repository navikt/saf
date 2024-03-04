package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode;

import java.util.Map;

public final class GeografiReason extends AbacDenyReason {

	public GeografiReason(Map<String, String> advices) {
		super(advices, AbacDenyReasonCode.GEOGRAFI);
	}

	public String getHumanReadableDenyReason() {
		return "Du har ikke tilgang til brukeren fordi han / hun er folkeregistrert i et geografisk område du ikke har tilgang til." + MAA_HA_GEOGRAFI;
	}
}