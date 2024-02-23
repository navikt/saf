package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode;

import java.util.Map;

public final class FortroligAdresseReason extends AbacDenyReason {
	public FortroligAdresseReason(Map<String, String> advices) {
		super(advices, AbacDenyReasonCode.FORTROLIG_ADRESSE);
	}

	public String getHumanReadableDenyReason() {
		return "Du har ikke tilgang til brukeren fordi han / hun har fortrolig adresse." + MAA_HA_FORTROLIG_TILGANG;
	}
}