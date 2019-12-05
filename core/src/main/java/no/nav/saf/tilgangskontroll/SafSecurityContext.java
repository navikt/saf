package no.nav.saf.tilgangskontroll;

import static no.nav.saf.util.MDCUtility.addMdcData;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.exceptions.OidcAuthorizationException;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.jwt.JwtToken;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
public class SafSecurityContext {
	private static final String SERVICEUSER_PREFIX = "srv";
	private static final String AUTH_ERRORMESSAGE = "Autentiseringsmekanisme er ikke støttet. " +
			"Kun OIDC-token (JWT via OAuth 2.0) med header \"Authorization\" : \"Bearer {token}\" er tillatt.";
	private static final Map<String, Boolean> PRIVILEGIED_SERVICEUSERS = new HashMap<>();
	private static final String UNKNOWN_AUDIENCE = "unknownAudience";
	private static final String UNKNOWN_ISSUER = "unknownIssuer";
	private final String jwtToken;
	private final String oidcTokenBody;
	private final String subjectId;
	private String navCallid;
	private final String navConsumerId;
	private final boolean azureToken;
	private final DecodedJWT decodedJWT;
	private final Set<String> azureIssuers;
	private final String audience;
	private final String issuer;

	static {
		// Disse servicebrukerene får tilgang til å hente dokumentvarianter
		PRIVILEGIED_SERVICEUSERS.put("srvdokdistfordeling", true);
		PRIVILEGIED_SERVICEUSERS.put("srvdokdisteformidling", true);
		PRIVILEGIED_SERVICEUSERS.put("srvdokarkivproxy", true);
		PRIVILEGIED_SERVICEUSERS.put("srvtilbakemeldings", true);
	}

	SafSecurityContext(Set<String> azureIssuers,
					   String navCallidHeader,
					   String navConsumerIdHeader,
					   TokenValidationContext tokenValidationContext) {
		this.azureIssuers = azureIssuers;
		this.jwtToken = getFirstValidJwt(tokenValidationContext);
		this.decodedJWT = getDecodedJWT(jwtToken);
		this.oidcTokenBody = getPayloadFromToken(decodedJWT);
		this.subjectId = getSubjectFromToken(decodedJWT);
		this.azureToken = getIsAzureToken(decodedJWT);
		this.audience = getAudienceFromToken(decodedJWT);
		this.issuer = getIssuerFromToken(decodedJWT);
		// if zero, then executionId from graphQl is used.
		this.navCallid = trim(navCallidHeader);
		this.navConsumerId = determineNavConsumerId(trim(navConsumerIdHeader), audience);

		addMdcData(this.subjectId, this.navCallid, this.navConsumerId);
	}

	private String getPayloadFromToken(DecodedJWT decodedJWT){
		return decodedJWT.getPayload();
	}

	private String getFirstValidJwt(TokenValidationContext tokenValidationContext){
			 return tokenValidationContext.getFirstValidToken().map(JwtToken::getTokenAsString).orElse(null);
	}

	private DecodedJWT getDecodedJWT(final String oidcTokenBody) {
		if (oidcTokenBody == null) {
			return null;
		}

		try {
			return JWT.decode(oidcTokenBody);
		} catch(JWTDecodeException e) {
			return null;
		}
	}

	private String determineNavConsumerId(final String navConsumerIdHeader, final String audience) {
		if (isBlank(navConsumerIdHeader)) {
			if (isBlank(audience)) {
				return getSubjectId();
			} else {
				return audience;
			}
		} else {
			return navConsumerIdHeader;
		}
	}

	private String getSubjectFromToken(final DecodedJWT decodedJWT) {
		if(decodedJWT == null) {
			return null;
		}

		try {
			return decodedJWT.getSubject();
		} catch (JWTDecodeException e) {
			log.error("Kunne ikke utlede subject fra OIDC-Token i header.", e);
			return null;
		}
	}

	private boolean getIsAzureToken(final DecodedJWT decodedJWT) {
		if(decodedJWT == null) {
			return false;
		} else {
			return this.azureIssuers.contains(decodedJWT.getIssuer());
		}
	}

	private String getAudienceFromToken(final DecodedJWT decodedJWT) {
		if(decodedJWT == null) {
			return UNKNOWN_AUDIENCE;
		}
		try {
			return decodedJWT.getAudience().stream().findFirst().orElse(UNKNOWN_AUDIENCE);
		} catch (JWTDecodeException e) {
			log.error("Kunne ikke utlede audience fra OIDC-Token i header.", e);
			return null;
		}
	}

	private String getIssuerFromToken(final DecodedJWT decodedJWT) {
		if(decodedJWT == null) {
			return UNKNOWN_ISSUER;
		}
		try {
			return decodedJWT.getIssuer();
		} catch (JWTDecodeException e) {
			log.error("Kunne ikke utlede issuer fra OIDC-Token i header.", e);
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

	public String getAudience() {
		return audience;
	}

	public String getIssuer() {
		return issuer;
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

	public boolean isAzureToken() {
		return azureToken;
	}
}
