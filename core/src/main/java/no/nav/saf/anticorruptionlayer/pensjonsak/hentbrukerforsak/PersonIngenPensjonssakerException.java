package no.nav.saf.anticorruptionlayer.pensjonsak.hentbrukerforsak;

import no.nav.saf.exceptions.SafFunctionalException;
import org.springframework.http.HttpStatus;

import static no.nav.saf.util.LogSanitizer.sanitizeFnr;

public class PersonIngenPensjonssakerException extends SafFunctionalException {
	public PersonIngenPensjonssakerException(String message, HttpStatus httpStatus) {
		super(sanitizeFnr(message), httpStatus);
	}
}
