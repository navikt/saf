package no.nav.saf.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNAUTHORIZED, reason = "OIDC token could not be found.")
public class OidcTokenNotFoundException extends SafFunctionalException {

	public OidcTokenNotFoundException(String message, HttpStatus httpStatus) {
		super(message, httpStatus);
	}
}
