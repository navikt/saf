package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.DenyReasonCode;

import java.util.Map;

public final class SkjermingReason extends DenyReason {
	public SkjermingReason(String cause, String policy, String rule) {
		super(cause, policy, rule, DenyReasonCode.SKJERMING, null, null);
	}

	public SkjermingReason(Map<String, String> advices) {
		super(advices, DenyReasonCode.SKJERMING);
	}

	public String getHumanReadableDenyReason() {
		return "Du har ikke tilgang til journalpost / dokument fordi den er skjermet eller kassert." + FAGPOST;
	}
}