package no.nav.saf.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DokumentIkkeFunnetException extends SafFunctionalException {

	public DokumentIkkeFunnetException(String message) {
		super(message);
	}
}

