package no.nav.saf.tilgangskontroll.pep.reasons;

import lombok.AllArgsConstructor;
import lombok.Getter;
import no.nav.saf.tilgangskontroll.pep.DenyReasonCode;

import java.util.Map;

@Getter
@AllArgsConstructor
public abstract sealed class DenyReason permits EgenAnsattReason, HabilitetReason, EgenAnsattPartReason, FortroligAdresseReason, FortroligAdressePartReason,
		GeografiReason, JournalstatusReason, OrgnrNavStatReason, SkjermingReason, StrengtFortroligAdresseReason, StrengtFortroligAdresseUtlandReason,
		StrengtFortroligAdressePartReason, StrengtFortroligAdresseUtlandPartReason, TemaReason, AvsluttetSakReason, PersonUtlandReason, UkjentEllerTekniskReason {

	protected static final String MAA_HA_EGEN_ANSATT = " Arbeidet må i stedet utføres av en medarbeider med egen-ansatt-tilgang.";
	protected static final String MAA_HA_FORTROLIG_TILGANG = " Arbeidet må i stedet utføres av en medarbeider med tilgang til brukere med fortrolig adresse.";
	protected static final String VIKAFOSSEN = " Arbeidet må i stedet utføres av NAV Vikafossen (2103).";
	protected static final String MAA_HA_GEOGRAFI = " Arbeidet må i stedet utføres av en medarbeider med tilgang til brukeren.";
	protected static final String FAGPOST = " Arbeidet må i stedet utføres av NAV Fagpost (2950).";

	protected final String cause;
	protected final String policy;
	protected final String rule;
	protected final DenyReasonCode denyReasonCode;
	protected final String rawTilgangsmaskinenDenyReason;
	protected final String rawTilgangsmaskinenBegrunnelse;

	protected DenyReason(Map<String, String> advices, DenyReasonCode denyReasonCode) {
		this(advices.get("cause"), advices.get("deny_policy"), advices.get("deny_rule"), denyReasonCode, null, null);
	}

	protected DenyReason(DenyReasonCode denyReasonCode, String rawTilgangsmaskinenDenyReason, String rawTilgangsmaskinenBegrunnelse) {
		this(null, null, null, denyReasonCode, rawTilgangsmaskinenDenyReason, rawTilgangsmaskinenBegrunnelse);
	}

	public String toString() {
		return "cause=" + getCause() + ", deny_policy=" + getPolicy() + ", deny_rule=" + getRule() +
			   ", reason_code=" + getDenyReasonCode().code + ", tilgangsmaskinenCode=" + rawTilgangsmaskinenDenyReason;
	}

	public abstract String getHumanReadableDenyReason();
}