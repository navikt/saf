package no.nav.saf.query.dokumentoversikt.fagsak;

import no.nav.saf.exceptions.SafFunctionalException;
import org.springframework.http.HttpStatus;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class UnsupportedFagsakSystemException extends SafFunctionalException {

	UnsupportedFagsakSystemException(String message) {
		super(message, HttpStatus.BAD_REQUEST);
	}
}
