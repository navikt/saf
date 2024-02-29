package no.nav.saf.exceptions;

import no.nav.saf.graphql.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class JournalpostIkkeFunnetException extends SafFunctionalException {

	public JournalpostIkkeFunnetException(String message) {
		super(message, ErrorCode.NOT_FOUND);
	}
}

