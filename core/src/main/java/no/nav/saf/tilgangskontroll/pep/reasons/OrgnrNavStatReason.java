package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.DenyReasonCode;

public final class OrgnrNavStatReason extends DenyReason {
	public OrgnrNavStatReason(String cause, String policy, String rule) {
		super(cause, policy, rule, DenyReasonCode.ORGNR_NAV_STAT, null, null);
	}

	public String getHumanReadableDenyReason() {
		return "Du har ikke tilgang fordi organisasjonsnummeret tilhører NAV." + MAA_HA_EGEN_ANSATT;
	}
}