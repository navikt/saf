package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.DenyReasonCode;

public final class PersonUtlandReason extends DenyReason {
	public PersonUtlandReason(String rawTilgangsmaskinenDenyReason, String rawTilgangsmaskinenBegrunnelse) {
		super(DenyReasonCode.PERSON_UTLAND, rawTilgangsmaskinenDenyReason, rawTilgangsmaskinenBegrunnelse);
	}

	public String getHumanReadableDenyReason() {
		return rawTilgangsmaskinenBegrunnelse + MAA_HA_GEOGRAFI;
	}
}
