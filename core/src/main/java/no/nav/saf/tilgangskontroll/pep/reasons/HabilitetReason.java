package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.DenyReasonCode;

public final class HabilitetReason extends DenyReason {

	public HabilitetReason(String rawTilgangsmaskinenDenyReason, String rawTilgangsmaskinenBegrunnelse) {
		super(DenyReasonCode.HABILITET, rawTilgangsmaskinenDenyReason, rawTilgangsmaskinenBegrunnelse);
	}

	@Override
	public String getHumanReadableDenyReason() {
		return rawTilgangsmaskinenDenyReason;
	}
}
