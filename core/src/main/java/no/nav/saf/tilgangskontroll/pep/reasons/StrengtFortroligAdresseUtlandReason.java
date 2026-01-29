package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.DenyReasonCode;

import java.util.Map;

public final class StrengtFortroligAdresseUtlandReason extends DenyReason {
	public StrengtFortroligAdresseUtlandReason(Map<String, String> advices) {
		super(advices, DenyReasonCode.STRENGT_FORTROLIG_ADRESSE_UTLAND);
	}

	public StrengtFortroligAdresseUtlandReason(String rawTilgangsmaskinenDenyReason, String rawTilgangmaskinenBegrunnelse) {
		super(DenyReasonCode.STRENGT_FORTROLIG_ADRESSE_UTLAND, rawTilgangsmaskinenDenyReason, rawTilgangmaskinenBegrunnelse);
	}

	public String getHumanReadableDenyReason() {
		return "Du har ikke tilgang til brukeren fordi han / hun har strengt fortrolig adresse utland." + VIKAFOSSEN;
	}
}