package no.nav.saf.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UgyldigInputException extends SafFunctionalException {

	public UgyldigInputException(String message) {
		super(message);
	}
}

