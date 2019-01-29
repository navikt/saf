package no.nav.saf.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class AbacException extends SafTechnicalException {

	public AbacException(String message, Throwable cause) {
		super(message, cause);
	}
}
