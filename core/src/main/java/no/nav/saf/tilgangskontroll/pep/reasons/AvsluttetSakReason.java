package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.DenyReasonCode;

import java.util.Map;

import static java.util.Collections.emptyMap;

public final class AvsluttetSakReason extends DenyReason {

	public AvsluttetSakReason() {
		this(emptyMap());
	}

	public AvsluttetSakReason(Map<String, String> advices) {
		super(advices, DenyReasonCode.AVSLUTTET_SAK);
	}

	@Override
	public String getHumanReadableDenyReason() {
		return "Du har ikke tilgang til journalpost / dokument fordi den er tilknyttet en avsluttet sak." +
				" Arbeidet må i stedet utføres av en medarbeider med tilgang til historiske saker.";
	}
}
