package no.nav.saf.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class HentdokumentTilgangskontrollException extends SafFunctionalException {
	private final String denyReason;

	public HentdokumentTilgangskontrollException(String message) {
		super(message);
		this.denyReason = null;
	}

	public HentdokumentTilgangskontrollException(String message, String denyReason) {
		super("Avvist av SAF tilgangskontroll: " + message);
		this.denyReason = denyReason;
	}

	public String getDenyReason() {
		return denyReason;
	}
}
