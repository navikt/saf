package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode;

import java.util.Map;

public final class EgenAnsattReason extends AbacDenyReason {
	public EgenAnsattReason(Map<String, String> advices) {
		super(advices, AbacDenyReasonCode.EGEN_ANSATT);
	}

	public String getHumanReadableDenyReason() {
		return "Du har ikke tilgang til brukeren fordi vedkommende er NAV-ansatt, eller er i nær familie med en NAV-ansatt." + MAA_HA_EGEN_ANSATT;
	}
}