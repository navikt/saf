package no.nav.saf.tilgangskontroll;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.exceptions.OidcAuthorizationException;

import java.util.Optional;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
public class SafRequestContext {
	private static final String OIDC_TOKEN_PREFIX = "Bearer ";
	private final Optional<String> oidcTokenBody;

	public SafRequestContext(String authorizationHeader) {
		this.oidcTokenBody = getOidcTokenBody(authorizationHeader);
	}

	private Optional<String> getOidcTokenBody(String authorizationHeader) {
		if(!authorizationHeader.startsWith(OIDC_TOKEN_PREFIX)) {
			return Optional.empty();
		}
		try {
			return Optional.of(JWT.decode(authorizationHeader.split(OIDC_TOKEN_PREFIX)[1]).getPayload());
		} catch (JWTDecodeException e) {
			log.error("Konsument satte ugyldig OIDC-Token i header.", e);
			return Optional.empty();
		}
	}

	public String getOidcTokenBody() {
		return oidcTokenBody.orElseThrow(() -> new OidcAuthorizationException("Autentiseringsmekanisme er ikke støttet. " +
				"Kun OIDC-token (JWT via OAuth 2.0) med header \"Authorization\" : \"Bearer {token}\" er tillatt."));
	}
}
