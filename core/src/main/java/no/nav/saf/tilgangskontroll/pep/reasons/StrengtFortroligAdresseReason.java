package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.DenyReasonCode;

import java.util.Map;

public final class StrengtFortroligAdresseReason extends DenyReason {
	public StrengtFortroligAdresseReason(Map<String, String> advices) {
		super(advices, DenyReasonCode.STRENGT_FORTROLIG_ADRESSE);
	}

	public StrengtFortroligAdresseReason(String rawTilgangsmaskinenDenyReason, String rawTilgangmaskinenBegrunnelse) {
		super(DenyReasonCode.STRENGT_FORTROLIG_ADRESSE, rawTilgangsmaskinenDenyReason, rawTilgangmaskinenBegrunnelse);
	}

	public String getHumanReadableDenyReason() {
		return "Du har ikke tilgang til brukeren fordi han / hun har strengt fortrolig adresse." + VIKAFOSSEN;
	}
}