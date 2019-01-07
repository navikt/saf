package no.nav.saf.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UgyldigArkivsaksystemException extends SafFunctionalException {

	public UgyldigArkivsaksystemException(String message) {
		super(message);
	}
}

