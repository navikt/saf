package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.DenyReasonCode;

import java.util.Map;

public final class FortroligAdressePartReason extends DenyReason {

	public FortroligAdressePartReason(Map<String, String> advices) {
		super(advices, DenyReasonCode.FORTROLIG_ADRESSE_PART);
	}

	public String getHumanReadableDenyReason() {
		return "Du har ikke tilgang til journalpost / dokument fordi den er journalført mot en fagsak der en av partene har fortrolig adresse." + MAA_HA_FORTROLIG_TILGANG;
	}
}