package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.DenyReasonCode;

import java.util.Map;

public final class StrengtFortroligAdressePartReason extends DenyReason {
	public StrengtFortroligAdressePartReason(Map<String, String> advices) {
		super(advices, DenyReasonCode.STRENGT_FORTROLIG_ADRESSE_PART);
	}

	public String getHumanReadableDenyReason() {
		return "Du har ikke tilgang til journalpost / dokument fordi den er journalført mot en fagsak der en av partene har strengt fortrolig adresse." + VIKAFOSSEN;
	}
}