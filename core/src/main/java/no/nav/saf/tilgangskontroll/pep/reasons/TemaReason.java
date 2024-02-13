package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.domain.kode.Tema;
import no.nav.saf.tilgangskontroll.pep.AbacAnswer;

import java.util.Map;

public final class TemaReason extends AbacDenyReason {
	private final Tema tema;
	public TemaReason(String cause, String policy, String rule, Tema tema) {
		super(cause, policy, rule, AbacAnswer.AbacDenyReasonCode.TEMA);
		this.tema = tema;
	}

	public TemaReason(Map<String,String> advices, Tema tema) {
		super(advices, AbacAnswer.AbacDenyReasonCode.TEMA);
		this.tema = tema;
	}

	public String getTemaForHumanDisplay() {
		return tema != null ? tema.name() : "[Ukjent tema]";
	}
}