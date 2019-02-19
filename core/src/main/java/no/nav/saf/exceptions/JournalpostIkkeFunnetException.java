package no.nav.saf.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class JournalpostIkkeFunnetException extends SafFunctionalException {

	public JournalpostIkkeFunnetException(String message) {
		super(message);
	}
}

