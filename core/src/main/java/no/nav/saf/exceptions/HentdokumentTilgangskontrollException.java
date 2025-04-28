package no.nav.saf.exceptions;

import no.nav.saf.tilgangskontroll.pep.PepAnswer;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class HentdokumentTilgangskontrollException extends TilgangskontrollException {

	public HentdokumentTilgangskontrollException(String message, PepAnswer pepAnswer) {
		super(message, pepAnswer);
	}
}
