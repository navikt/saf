package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode;

import java.util.Map;

public final class OrgnrNavStatReason extends AbacDenyReason {
	public OrgnrNavStatReason(Map<String, String> advices) {
		super(advices, AbacDenyReasonCode.ORGNR_NAV_STAT);
	}

	public String getHumanReadableDenyReason() {
		return "Du har ikke tilgang fordi organisasjonsnummeret tilhører NAV. " + MAA_HA_EGEN_ANSATT;
	}
}