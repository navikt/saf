package no.nav.saf.tilgangskontroll;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.exceptions.OidcAuthorizationException;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
public class SafSecurityContext {
	private static final String OIDC_TOKEN_PREFIX = "Bearer ";
	private final String oidcTokenBody;

	SafSecurityContext(String authorizationHeader) {
		this.oidcTokenBody = getOidcTokenBody(authorizationHeader);
	}

	private String getOidcTokenBody(String authorizationHeader) {
		if(authorizationHeader == null || !authorizationHeader.startsWith(OIDC_TOKEN_PREFIX)) {
			return null;
		}
		try {
			return JWT.decode(authorizationHeader.split(OIDC_TOKEN_PREFIX)[1]).getPayload();
		} catch (JWTDecodeException e) {
			log.error("Konsument satte ugyldig OIDC-Token i header.", e);
			return null;
		}
	}

	public String getOidcTokenBody() {
		if(oidcTokenBody == null) {
			throw new OidcAuthorizationException("Autentiseringsmekanisme er ikke støttet. " +
					"Kun OIDC-token (JWT via OAuth 2.0) med header \"Authorization\" : \"Bearer {token}\" er tillatt.");
		} else {
			return oidcTokenBody;
		}
	}
}
