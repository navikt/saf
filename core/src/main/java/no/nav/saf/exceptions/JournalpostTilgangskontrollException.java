package no.nav.saf.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Ingen tilgang til journalposten")
public class JournalpostTilgangskontrollException extends SafFunctionalException {

	public JournalpostTilgangskontrollException(String message) {
		super(message);
	}
}
