package no.nav.saf.exceptions;

import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.FORBIDDEN;

@ResponseStatus(FORBIDDEN)
public class AuthorizationException extends SafFunctionalException {

	public AuthorizationException(String message) {
		super(message, FORBIDDEN);
	}

	public AuthorizationException(String message, Throwable cause) {
		super(message, cause, FORBIDDEN);
	}
}
