package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.DenyReasonCode;

import java.util.Map;

public final class FortroligAdresseReason extends DenyReason {
	public FortroligAdresseReason(Map<String, String> advices) {
		super(advices, DenyReasonCode.FORTROLIG_ADRESSE);
	}

	public FortroligAdresseReason(String rawTilgangsmaskinenDenyReason, String rawTilgangmaskinenBegrunnelse) {
		super(DenyReasonCode.FORTROLIG_ADRESSE, rawTilgangsmaskinenDenyReason, rawTilgangmaskinenBegrunnelse);
	}

	public String getHumanReadableDenyReason() {
		return "Du har ikke tilgang til brukeren fordi han / hun har fortrolig adresse." + MAA_HA_FORTROLIG_TILGANG;
	}
}