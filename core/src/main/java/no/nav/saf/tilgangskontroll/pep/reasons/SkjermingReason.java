package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class SkjermingReason extends AbacDenyReason {
	public SkjermingReason(String cause, String policy, String rule) {
		super(cause, policy, rule, AbacDenyReasonCode.SKJERMING);
	}

	public SkjermingReason(Map<String,String> advices) {
		super(advices, AbacDenyReasonCode.SKJERMING);
	}

	public String getHumanReadableDenyReason() {
		return "Du har ikke tilgang til journalpost / dokument fordi den er skjermet eller kassert." + FAGPOST;
	}
}