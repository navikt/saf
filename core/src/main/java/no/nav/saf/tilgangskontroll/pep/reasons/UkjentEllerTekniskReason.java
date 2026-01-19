package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.DenyReasonCode;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public final class UkjentEllerTekniskReason extends DenyReason {
	public UkjentEllerTekniskReason(String cause, String policy, String rule) {
		super(cause, policy, rule, DenyReasonCode.UKJENT, null, null);
	}

	public UkjentEllerTekniskReason() {
		this(null, null, null);
	}

	public UkjentEllerTekniskReason(String rawTilgangsmaskinenDenyReason, String rawTilgangmaskinenBegrunnelse) {
		super(DenyReasonCode.UKJENT, rawTilgangsmaskinenDenyReason, rawTilgangmaskinenBegrunnelse);
	}

	public String getHumanReadableDenyReason() {
		if (isNotBlank(rawTilgangsmaskinenBegrunnelse)) {
			return rawTilgangsmaskinenBegrunnelse;
		}
		return "Du har blitt nektet tilgang av en ukjent grunn, eller på grunn av teknisk feil. " +
				"Prøv på nytt om litt. Om du fortsatt ikke får tilgang må du melde inn en sak til brukerstøtte i Porten.";
	}
}