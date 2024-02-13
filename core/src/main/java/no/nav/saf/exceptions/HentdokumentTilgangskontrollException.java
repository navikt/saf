package no.nav.saf.exceptions;

import no.nav.saf.tilgangskontroll.pep.AbacAnswer;
import no.nav.saf.tilgangskontroll.pep.reasons.AbacDenyReason;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class HentdokumentTilgangskontrollException extends SafFunctionalException {
	private final String denyReason;
	private final AbacDenyReason abacDenyReason;

	// må kanskje ha inn en enum her feks.
	public HentdokumentTilgangskontrollException(String message, AbacAnswer abacAnswer) {
		super("Avvist av SAF tilgangskontroll: " + message);
		this.denyReason = abacAnswer.getDenyReasonSporing();
		this.abacDenyReason = abacAnswer.getAbacDenyReason();
	}

	public String getDenyReason() {
		return denyReason;
	}

	public AbacDenyReason getAbacDenyReason() {
		return abacDenyReason;
	}
}
