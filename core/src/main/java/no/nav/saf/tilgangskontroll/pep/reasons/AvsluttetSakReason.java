package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode;

import java.util.Map;

import static java.util.Collections.emptyMap;

public final class AvsluttetSakReason extends AbacDenyReason {

	public AvsluttetSakReason() {
		this(emptyMap());
	}

	public AvsluttetSakReason(Map<String, String> advices) {
		super(advices, AbacDenyReasonCode.AVSLUTTET_SAK);
	}

	@Override
	public String getHumanReadableDenyReason() {
		return "Du har ikke tilgang til journalpost / dokument fordi den er tilknyttet en avsluttet sak." +
				" Arbeidet må i stedet utføres av en medarbeider med tilgang til historiske saker.";
	}
}
