package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode;

public final class PersonUtlandReason extends AbacDenyReason {
	public PersonUtlandReason(String rawTilgangsmaskinenDenyReason, String rawTilgangsmaskinenBegrunnelse) {
		super(AbacDenyReasonCode.PERSON_UTLAND, rawTilgangsmaskinenDenyReason, rawTilgangsmaskinenBegrunnelse);
	}

	public String getHumanReadableDenyReason() {
		return rawTilgangsmaskinenBegrunnelse + MAA_HA_GEOGRAFI;
	}
}
