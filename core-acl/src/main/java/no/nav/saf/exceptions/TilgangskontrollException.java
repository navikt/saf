package no.nav.saf.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Ingen tilgang til dokumentet")
public class TilgangskontrollException extends SafFunctionalException {

	public TilgangskontrollException() {
	}
}
