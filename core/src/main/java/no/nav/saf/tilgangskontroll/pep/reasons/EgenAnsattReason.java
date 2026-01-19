package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.DenyReasonCode;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public final class EgenAnsattReason extends DenyReason {
	public EgenAnsattReason(Map<String, String> advices) {
		super(advices, DenyReasonCode.EGEN_ANSATT);
	}

	public EgenAnsattReason(String rawTilgangsmaskinenDenyReason, String rawTilgangsmaskinenBegrunnelse) {
		super(DenyReasonCode.EGEN_ANSATT, rawTilgangsmaskinenDenyReason, rawTilgangsmaskinenBegrunnelse);
	}

	public String getHumanReadableDenyReason() {
		if (isNotBlank(rawTilgangsmaskinenBegrunnelse)) {
			return rawTilgangsmaskinenBegrunnelse + MAA_HA_EGEN_ANSATT;
		}
		return "Du har ikke tilgang til brukeren fordi vedkommende er NAV-ansatt, eller er i nær familie med en NAV-ansatt." + MAA_HA_EGEN_ANSATT;
	}
}