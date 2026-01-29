package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.domain.kode.Tema;
import no.nav.saf.tilgangskontroll.pep.DenyReasonCode;

import java.util.Map;

public final class TemaReason extends DenyReason {
	private final Tema tema;

	public TemaReason(String cause, String policy, String rule, Tema tema) {
		super(cause, policy, rule, DenyReasonCode.TEMA, null, null);
		this.tema = tema;
	}

	public TemaReason(Map<String, String> advices, Tema tema) {
		super(advices, DenyReasonCode.TEMA);
		this.tema = tema;
	}

	public String getTemaForHumanDisplay() {
		return tema != null ? tema.getTemanavn() : "[Ukjent tema]";
	}

	public String getHumanReadableDenyReason() {
		return "Du har ikke tilgang til journalpost / dokument fordi du mangler tilgang til tema \"" + getTemaForHumanDisplay() +
				"\". Arbeidet må i stedet utføres av en medarbeider/system med tilgang til temaet.";
	}
}