package no.nav.saf.exceptions;

import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class HentdokumentTilgangskontrollException extends SafFunctionalException {
	private final XacmlResponse xacmlResponse;

	public HentdokumentTilgangskontrollException(String message) {
		super(message);
		this.xacmlResponse = null;
	}

	public HentdokumentTilgangskontrollException(String message, XacmlResponse xacmlResponse) {
		super("Avvist av SAF ABAC policy " + message);
		this.xacmlResponse = xacmlResponse;
	}

	public XacmlResponse getXacmlResponse() {
		return xacmlResponse;
	}
}
