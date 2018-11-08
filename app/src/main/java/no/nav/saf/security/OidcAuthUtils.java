package no.nav.saf.security;

import static java.lang.String.format;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import no.nav.saf.exceptions.OidcAuthentificationException;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Optional;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public final class OidcAuthUtils {

	private static final String BEARER_TOKEN_PREFIX = "Bearer ";

	public static String getOidcToken(HttpHeaders httpHeaders) {
		String oidcToken = Optional.ofNullable(getAuthorizationHeader(httpHeaders))
				.filter(e -> e.startsWith(BEARER_TOKEN_PREFIX))
				.map(e -> e.replaceFirst(BEARER_TOKEN_PREFIX, ""))
				.orElseThrow(() -> new OidcAuthentificationException("Kunne ikke hente ut Authorization header fra request. Forventet format på Authorization header er: \"Bearer *OIDC-token*\""));

		try {
			return JWT.decode(oidcToken).getPayload();
		} catch (JWTDecodeException e) {
			throw new OidcAuthentificationException("Dekoding av OIDC-token feilet. Vennligst bruk et oidc-token med gyldig format. Gyldig format på oidc-token er: \"header.body.signature\"");
		}
	}

	private static String getAuthorizationHeader(HttpHeaders httpHeaders) {
		List<String> authorization = Optional.ofNullable(
				httpHeaders.get(HttpHeaders.AUTHORIZATION))
				.orElseThrow(() -> new OidcAuthentificationException("Authorization header mangler på request. Forventet format på Authorization header er: \"Bearer *OIDC-token*\""));

		if (authorization.size() != 1) {
			throw new OidcAuthentificationException(format("Forventet kun én Autorization header på request; fikk %s. Formatet på Authorization header skal være: \"Bearer *OIDC-token*\"",
					authorization.size()));
		}

		return authorization.get(0);

	}
}
