package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.domain.kode.Tema;
import no.nav.saf.tilgangskontroll.pep.AbacDenyReasonCode;

import java.util.Map;

public final class TemaReason extends AbacDenyReason {
	private final Tema tema;

	public TemaReason(String cause, String policy, String rule, Tema tema) {
		super(cause, policy, rule, AbacDenyReasonCode.TEMA, null, null);
		this.tema = tema;
	}

	public TemaReason(Map<String, String> advices, Tema tema) {
		super(advices, AbacDenyReasonCode.TEMA);
		this.tema = tema;
	}

	public String getTemaForHumanDisplay() {
		return tema != null ? tema.getTemanavn() : "[Ukjent tema]";
	}

	public String getHumanReadableDenyReason() {
		return "Du har ikke tilgang til journalpost / dokument fordi du mangler tilgang til tema \"" + getTemaForHumanDisplay() +
				"\". Arbeidet må i stedet utføres av en medarbeider med tilgang til temaet.";
	}
}