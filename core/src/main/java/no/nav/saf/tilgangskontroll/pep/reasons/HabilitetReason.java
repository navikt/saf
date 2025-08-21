package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode;

public final class HabilitetReason extends AbacDenyReason {

	public HabilitetReason(String rawTilgangsmaskinenDenyReason, String rawTilgangsmaskinenBegrunnelse) {
		super(AbacDenyReasonCode.HABILITET, rawTilgangsmaskinenDenyReason, rawTilgangsmaskinenBegrunnelse);
	}

	@Override
	public String getHumanReadableDenyReason() {
		return rawTilgangsmaskinenDenyReason;
	}
}
