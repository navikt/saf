package no.nav.saf.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Autentiseringsmekanisme er ikke støttet. " +
		"Kun OIDC-token (JWT via OAuth 2.0) med header \"Authorization\" : \"Bearer {token}\" er tillatt.")
public class UnsupportedAuthorizationException extends RuntimeException {

}
