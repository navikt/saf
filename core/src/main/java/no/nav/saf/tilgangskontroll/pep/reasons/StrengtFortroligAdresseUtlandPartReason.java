package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.DenyReasonCode;

import java.util.Map;

public final class StrengtFortroligAdresseUtlandPartReason extends DenyReason {
	public StrengtFortroligAdresseUtlandPartReason(Map<String, String> advices) {
		super(advices, DenyReasonCode.STRENGT_FORTROLIG_ADRESSE_UTLAND_PART);
	}

	public String getHumanReadableDenyReason() {
		return "Du har ikke tilgang til journalpost / dokument fordi den er journalført mot en fagsak der en av partene har strengt fortrolig adresse utland." + VIKAFOSSEN;
	}
}