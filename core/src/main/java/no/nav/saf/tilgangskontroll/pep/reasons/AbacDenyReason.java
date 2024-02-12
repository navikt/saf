package no.nav.saf.tilgangskontroll.pep.reasons;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import no.nav.saf.tilgangskontroll.pep.AbacAnswer;

import java.util.Map;

@Getter
@AllArgsConstructor
public abstract sealed class AbacDenyReason permits EgenAnsattReason, EgenAnsattPartReason, FortroligAdresseReason, FortroligAdressePartReason,
		GeografiReason, JournalstatusReason, OrgnrNavStatReaason, SkjermingReason, StrengtFortroligAdresseReason, StrengtFortroligAdresseUtlandReason,
		StrengtFortroligAdressePartReason, StrengtFortroligAdresseUtlandPartReason, TemaReason, UkjentReason {

	protected final String cause;
	protected final String policy;
	protected final String rule;
	protected final AbacAnswer.AbacDenyReasonCode abacDenyReasonCode;

	public AbacDenyReason(Map<String,String> advices, AbacAnswer.AbacDenyReasonCode abacDenyReasonCode) {
		this(null, advices.get("deny_policy"), advices.get("deny_rule"), abacDenyReasonCode);
	}

	public String toString() {
		return "cause=" + getCause() + ", deny_policy=" + getPolicy() + ", deny_rule=" + getRule() + ", reason_code=" + getAbacDenyReasonCode().code;
	}
}

/*
EgenAnsatt, EgenAnsattPart, FortroligAdresse, FortroligAdressePart, Geografi, Journalstatus, OrgnrNavStat, Skjerming,
StrengtFortroligAdresse, StrengtFortroligAdresseUtland, StrengtFortroligAdressePart, StrengtFortroligAdresseUtlandPart, Tema, Ukjent
*/