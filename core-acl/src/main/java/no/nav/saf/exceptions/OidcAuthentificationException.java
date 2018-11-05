package no.nav.saf.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNAUTHORIZED)
public class OidcAuthentificationException extends SafFunctionalException {

	public OidcAuthentificationException(String message) {
		super(message);
	}
}
