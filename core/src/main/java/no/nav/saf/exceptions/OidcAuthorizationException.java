package no.nav.saf.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class OidcAuthorizationException extends SafFunctionalException {

	public OidcAuthorizationException(String message) {
		super(message);
	}

	public OidcAuthorizationException(String message, Throwable cause) {
		super(message, cause);
	}
}
