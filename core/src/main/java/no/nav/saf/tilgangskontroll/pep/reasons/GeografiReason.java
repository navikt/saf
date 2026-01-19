package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.DenyReasonCode;

import java.util.Map;

public final class GeografiReason extends DenyReason {

	public GeografiReason(Map<String, String> advices) {
		super(advices, DenyReasonCode.GEOGRAFI);
	}

	public GeografiReason(String rawTilgangsmaskinenDenyReason, String rawTilgangmaskinenBegrunnelse) {
		super(DenyReasonCode.GEOGRAFI, rawTilgangsmaskinenDenyReason, rawTilgangmaskinenBegrunnelse);
	}

	public String getHumanReadableDenyReason() {
		return "Du har ikke tilgang til brukeren fordi han / hun er folkeregistrert i et geografisk område du ikke har tilgang til." + MAA_HA_GEOGRAFI;
	}
}