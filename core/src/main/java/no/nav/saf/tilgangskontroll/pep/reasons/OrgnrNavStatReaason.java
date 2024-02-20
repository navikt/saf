package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public final class OrgnrNavStatReaason extends AbacDenyReason {
	public OrgnrNavStatReaason(Map<String,String> advices) {
		super(advices, AbacDenyReasonCode.ORGNR_NAV_STAT);
	}

	public String getHumanReadableDenyReason() {
		return "Du har ikke tilgang fordi organisasjonsnummeret tilhører NAV. " + MAA_HA_EGEN_ANSATT;
	}
}