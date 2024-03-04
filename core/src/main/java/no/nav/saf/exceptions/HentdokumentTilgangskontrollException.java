package no.nav.saf.exceptions;

import no.nav.saf.tilgangskontroll.pep.AbacAnswer;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class HentdokumentTilgangskontrollException extends TilgangskontrollException {

	public HentdokumentTilgangskontrollException(String message, AbacAnswer abacAnswer) {
		super(message, abacAnswer);
	}
}
