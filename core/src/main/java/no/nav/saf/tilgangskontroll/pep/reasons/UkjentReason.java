package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode;
import org.jetbrains.annotations.NotNull;

public final class UkjentReason extends AbacDenyReason {
	public UkjentReason(String cause, String policy, String rule) {
		super(cause, policy, rule, AbacDenyReasonCode.UKJENT);
	}

	public UkjentReason() {
		this(null, null, null);
	}

	public String getHumanReadableDenyReason() {
		return "Du har blitt nektet tilgang av en ukjent grunn, eller på grunn av teknisk feil. " +
				"Prøv på nytt om litt. Om du fortsatt ikke får tilgang må du melde inn en sak til brukerstøtte i Porten.";
	}
}