package no.nav.saf.exceptions;

import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Ingen tilgang til dokumentet")
public class HentdokumentTilgangskontrollException extends SafFunctionalException {
	private final XacmlResponse xacmlResponse;

	public HentdokumentTilgangskontrollException(String message, XacmlResponse xacmlResponse) {
		super(message);
		this.xacmlResponse = xacmlResponse;
	}

	public XacmlResponse getXacmlResponse() {
		return xacmlResponse;
	}
}
