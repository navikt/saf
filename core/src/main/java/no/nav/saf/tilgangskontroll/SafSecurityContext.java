package no.nav.saf.tilgangskontroll;

import static no.nav.saf.util.MDCUtility.addMdcData;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.exceptions.OidcAuthorizationException;
import no.nav.saf.tilgangskontroll.validation.OidcValidatorTool;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
public class SafSecurityContext {
	public static final String SERVICEUSER_PREFIX = "srv";
	private static final String OIDC_TOKEN_PREFIX = "Bearer ";
	public static final String AUTH_ERRORMESSAGE = "Autentiseringsmekanisme er ikke støttet. " +
			"Kun OIDC-token (JWT via OAuth 2.0) med header \"Authorization\" : \"Bearer {token}\" er tillatt.";
	private static final Map<String, Boolean> PRIVILEGIED_SERVICEUSERS = new HashMap<>();
	private final String oidcTokenBody;
	private final String subjectId;
	private String navCallid;
	private final String navConsumerId;
	private final OidcValidatorTool oidcValidatorTool;

	static {
		// Disse servicebrukerene får tilgang til å hente dokumentvarianter
		PRIVILEGIED_SERVICEUSERS.put("srvdokdistfordeling", true);
		PRIVILEGIED_SERVICEUSERS.put("srvdokdisteformidling", true);
		PRIVILEGIED_SERVICEUSERS.put("srvdokarkivproxy", true);
		PRIVILEGIED_SERVICEUSERS.put("srvtilbakemeldings", true);
	}

	SafSecurityContext(String authorizationHeader,
					   String navCallidHeader,
					   String navConsumerIdHeader,
					   OidcValidatorTool oidcValidatorTool) {
		this.oidcValidatorTool = oidcValidatorTool;
		this.subjectId = getSubjectFromToken(authorizationHeader);
		this.oidcTokenBody = getOidcTokenBody(authorizationHeader);
		final String audience = getAudienceFromToken(authorizationHeader);
		// if zero, then executionId from graphQl is used.
		this.navCallid = trim(navCallidHeader);
		this.navConsumerId = determineNavConsumerId(trim(navConsumerIdHeader), audience);

		addMdcData(this.subjectId, this.navCallid, this.navConsumerId);
	}

	private String determineNavConsumerId(final String navConsumerIdHeader, final String audience) {
		if (isBlank(navConsumerIdHeader)) {
			if(isBlank(audience)) {
				return getSubjectId();
			} else {
				return audience;
			}
		} else {
			return navConsumerIdHeader;
		}
	}

	private String getOidcTokenBody(String authorizationHeader) {
		if (isNotValidAuthorizationHeader(authorizationHeader)) {
			return null;
		}

		if (oidcValidatorTool.validate(authorizationHeader)) {
			return JWT.decode(authorizationHeader.split(OIDC_TOKEN_PREFIX)[1]).getPayload();
		} else {
			return null;
		}

	}

	private String getSubjectFromToken(String authorizationHeader) {
		if (isNotValidAuthorizationHeader(authorizationHeader)) {
			return null;
		}
		try {
			return JWT.decode(authorizationHeader.split(OIDC_TOKEN_PREFIX)[1]).getSubject();
		} catch (JWTDecodeException e) {
			log.error("Kunne ikke utlede subject fra OIDC-Token i header.", e);
			return null;
		}
	}

	private String getAudienceFromToken(String authorizationHeader) {
		if (isNotValidAuthorizationHeader(authorizationHeader)) {
			return null;
		}
		try {
			return JWT.decode(authorizationHeader.split(OIDC_TOKEN_PREFIX)[1]).getAudience().stream().findFirst().orElse("unknownAudience");
		} catch (JWTDecodeException e) {
			log.error("Kunne ikke utlede audience fra OIDC-Token i header.", e);
			return null;
		}
	}

	public String getNavCallid() {
		return navCallid;
	}

	public String getNavConsumerId() {
		return navConsumerId;
	}

	public String getOidcTokenBody() {
		if (oidcTokenBody == null) {
			throw new OidcAuthorizationException(AUTH_ERRORMESSAGE);
		} else {
			return oidcTokenBody;
		}
	}

	public String getSubjectId() {
		if (subjectId == null) {
			throw new OidcAuthorizationException(AUTH_ERRORMESSAGE);
		} else {
			return subjectId;
		}
	}

	public boolean isServiceUser() {
		if (subjectId == null) {
			throw new OidcAuthorizationException(AUTH_ERRORMESSAGE);
		} else {
			return subjectId.toLowerCase().startsWith(SERVICEUSER_PREFIX);
		}
	}

	public boolean isPrivilegiedServiceUser() {
		return isServiceUser() && PRIVILEGIED_SERVICEUSERS.containsKey(subjectId.toLowerCase());
	}

	private boolean isNotValidAuthorizationHeader(String authorizationHeader) {
		return authorizationHeader == null || !authorizationHeader.startsWith(OIDC_TOKEN_PREFIX);
	}
}
