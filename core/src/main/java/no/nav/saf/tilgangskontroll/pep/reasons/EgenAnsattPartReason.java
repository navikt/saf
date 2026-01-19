package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.DenyReasonCode;

import java.util.Map;

public final class EgenAnsattPartReason extends DenyReason {
	public EgenAnsattPartReason(Map<String, String> advices) {
		super(advices, DenyReasonCode.EGEN_ANSATT_PART);
	}

	public String getHumanReadableDenyReason() {
		return "Du har ikke tilgang til journalpost / dokument fordi den er journalført mot en fagsak der en av partene er NAV-ansatt." + MAA_HA_EGEN_ANSATT;
	}
}