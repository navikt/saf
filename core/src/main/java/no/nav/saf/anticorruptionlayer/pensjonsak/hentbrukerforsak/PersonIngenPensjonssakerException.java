package no.nav.saf.anticorruptionlayer.pensjonsak.hentbrukerforsak;

import no.nav.saf.exceptions.SafFunctionalException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import static no.nav.saf.util.LogSanitizer.sanitizeFnr;

public class PersonIngenPensjonssakerException extends SafFunctionalException {
	public PersonIngenPensjonssakerException(String message, HttpStatusCode httpStatus) {
		super(sanitizeFnr(message), httpStatus);
	}
}
