package no.nav.saf.tilgangskontroll;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.exceptions.AuthorizationException;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
public class SafSecurityContext {
	private static final String ISSUER_REST_STS = "reststs";
	private static final String ISSUER_OPENAM = "openam";
	private static final String ISSUER_AZUREV1 = "azurev1";
	private static final String ISSUER_AZUREV2 = "azurev2";
	// JWT claims. https://datatracker.ietf.org/doc/html/rfc7519#section-4.1
	static final String JWT_CLAIM_AUD = "aud";
	// Azure claims. https://docs.microsoft.com/en-us/azure/active-directory/develop/access-tokens#payload-claims
	static final String AZURE_CLAIM_AZP = "azp";
	static final String AZURE_CLAIM_OID = "oid";
	static final String AZURE_CLAIM_SUB = "sub";
	static final String AZURE_CLAIM_ROLES = "roles";
	// NAV custom Azure claim. https://doc.nais.io/security/auth/azure-ad/configuration/#extra
	static final String AZURE_NAV_CUSTOM_CLAIM_NAVIDENT = "NAVident";
	static final String AZURE_NAV_CUSTOM_CLAIM_AZP_NAME = "azp_name";

	private static final String SERVICEUSER_PREFIX = "srv";
	private static final String AUTH_ERRORMESSAGE = "Tilgang er avvist. " +
			"Ingen gyldig token på Authorization header. Token må være utsted av NAV onprem security-token-service, openam eller azure.";
	private static final String UKJENT_CONSUMER_ID = "ukjentConsumerId";
	private static final String UKJENT_USER_ID = "ukjentUserId";
	@Deprecated
	private static final String UKJENT_AUDIENCE = "ukjentAudience";
	@Deprecated
	private static final String OPENAM_UKJENT_AUDIENCE = "openamUkjentAudience";

	private final TokenValidationContext tokenValidationContext;
	private final JwtToken jwtToken;
	private final String cachedJwtPayload;
	private final boolean jwtIssuedByAzure;
	private final boolean jwtAzureClientCredentialFlow;
	private final List<String> jwtAzureRoles;
	private final Map<String, Boolean> privilegiedServiceusers;

	SafSecurityContext(TokenValidationContext tokenValidationContext,
					   Map<String, Boolean> privilegiedServiceusers) {
		this.jwtToken = tokenValidationContext.getFirstValidToken()
				.orElseThrow(() -> new AuthorizationException(AUTH_ERRORMESSAGE));
		this.tokenValidationContext = tokenValidationContext;
		this.privilegiedServiceusers = privilegiedServiceusers;
		// Payload fra JWT hentes ut en gang pga den blir hentet ut fra kontekst ofte.
		JWT jwt;
		try {
			jwt = JWTParser.parse(this.jwtToken.getTokenAsString());
		} catch (ParseException e) {
			throw new AuthorizationException("Kunne ikke parse JWT token.", e);
		}
		if (jwt instanceof SignedJWT) {
			this.cachedJwtPayload = ((SignedJWT) jwt).getPayload().toBase64URL().toString();
		} else {
			throw new AuthorizationException("Kun SignedJWT er støttet i saf.");
		}
		this.jwtIssuedByAzure = tokenValidationContext.hasTokenFor(ISSUER_AZUREV1) || tokenValidationContext.hasTokenFor(ISSUER_AZUREV2);
		this.jwtAzureClientCredentialFlow = isClientCredentialFlowToken(jwtToken);
		if (jwtIssuedByAzure) {
			this.jwtAzureRoles = findAzureRoles(jwtToken);
		} else {
			this.jwtAzureRoles = new ArrayList<>();
		}
	}

	/**
	 * Brukes av abac-saf policy decision point (PDP).
	 * Hentet ut en gang for ytelse.
	 *
	 * @return Payload delen av JWT
	 */
	public String getCachedJwtPayload() {
		return cachedJwtPayload;
	}

	/**
	 * Brukes av saf policy enforcement point (PEP).
	 * Lagres pga ytelse
	 *
	 * @return true hvis token er utsted av Azure, false ellers
	 */
	public boolean isJwtIssuedByAzure() {
		return jwtIssuedByAzure;
	}

	/**
	 * Om token er i kontekst av system eller bruker.
	 *
	 * @return true hvis token er utsted av REST-STS eller er Azure client-credential flow, ellers false
	 */
	public boolean isSystem() {
		return isRestStsSystemToken() || jwtAzureClientCredentialFlow;
	}

	/**
	 * Om servicebruker er privilegiert og kan hente ARKIV varianter (pdf).
	 *
	 * @return true hvis systemet står i listen med ENV-verdi: SAF_PRIVILEGIEDSERVICEUSERS, ellers false
	 */
	public boolean isPrivilegiedServiceUserWithArkivVariantAccess() {
		return isSystem() && privilegiedServiceusers.containsKey(jwtToken.getSubject().toLowerCase());
	}

	/**
	 * Om token er utsted av Azure i deres client credential flow.
	 *
	 * @return true hvis jwt er Azure client credential flow token.
	 */
	public boolean isJwtAzureClientCredentialFlow() {
		return jwtAzureClientCredentialFlow;
	}

	public boolean containsRole(String role) {
		return jwtAzureRoles.contains(role);
	}

	@Deprecated
	public String getIssuer() {
		return jwtToken.getIssuer();
	}

	@Deprecated
	public String getAudience() {
		try {
			return jwtToken.getJwtTokenClaims().getAsList(JWT_CLAIM_AUD).get(0);
		} catch (Exception e) {
			return UKJENT_AUDIENCE;
		}
	}

	protected String getConsumerId() {
		if (isRestStsSystemToken()) {
			return jwtToken.getSubject();
		} else if (isOpenAmBrukerToken()) {
			try {
				return jwtToken.getJwtTokenClaims().getAsList(JWT_CLAIM_AUD).get(0);
			} catch (Exception e) {
				return OPENAM_UKJENT_AUDIENCE;
			}
		} else if (jwtAzureClientCredentialFlow || isOnBehalfOfFlowToken()) {
			return findAzureAppnameClaim(jwtToken.getJwtTokenClaims());
		}
		return UKJENT_CONSUMER_ID;
	}

	protected String getUserId() {
		if (isRestStsSystemToken() || isOpenAmBrukerToken()) {
			return jwtToken.getSubject();
		} else if (isClientCredentialFlowToken(jwtToken)) {
			return findAzureAppnameClaim(jwtToken.getJwtTokenClaims());
		} else if (isOnBehalfOfFlowToken()) {
			if (jwtToken.getJwtTokenClaims().getAllClaims().containsKey(AZURE_NAV_CUSTOM_CLAIM_NAVIDENT)) {
				return jwtToken.getJwtTokenClaims().getStringClaim(AZURE_NAV_CUSTOM_CLAIM_NAVIDENT);
			} else {
				return jwtToken.getJwtTokenClaims().getStringClaim(AZURE_CLAIM_OID);
			}
		}
		return UKJENT_USER_ID;
	}

	protected boolean isRestStsSystemToken() {
		return tokenValidationContext.hasTokenFor(ISSUER_REST_STS)
				&& jwtToken.getSubject().toLowerCase().startsWith(SERVICEUSER_PREFIX);
	}

	protected boolean isOpenAmBrukerToken() {
		return tokenValidationContext.hasTokenFor(ISSUER_OPENAM);
	}

	protected boolean isOnBehalfOfFlowToken() {
		final JwtTokenClaims jwtTokenClaims = jwtToken.getJwtTokenClaims();
		return jwtTokenClaims.getStringClaim(AZURE_CLAIM_SUB) != null &&
				jwtTokenClaims.getStringClaim(AZURE_CLAIM_OID) != null &&
				!jwtTokenClaims.getStringClaim(AZURE_CLAIM_SUB).equals(jwtTokenClaims.getStringClaim(AZURE_CLAIM_OID));
	}

	protected boolean isClientCredentialFlowToken(JwtToken jwtToken) {
		if (isJwtIssuedByAzure()) {
			final JwtTokenClaims jwtTokenClaims = jwtToken.getJwtTokenClaims();
			return jwtTokenClaims.getStringClaim(AZURE_CLAIM_SUB) != null &&
					jwtTokenClaims.getStringClaim(AZURE_CLAIM_OID) != null &&
					jwtTokenClaims.getStringClaim(AZURE_CLAIM_SUB).equals(jwtTokenClaims.getStringClaim(AZURE_CLAIM_OID));
		} else {
			return false;
		}
	}

	protected String findAzureAppnameClaim(JwtTokenClaims jwtTokenClaims) {
		if (jwtTokenClaims.getAllClaims().containsKey(AZURE_NAV_CUSTOM_CLAIM_AZP_NAME)) {
			String azpnameClaim = jwtTokenClaims.getStringClaim(AZURE_NAV_CUSTOM_CLAIM_AZP_NAME);
			if (isNotBlank(azpnameClaim)) {
				return azpnameClaim;
			}
			return jwtTokenClaims.getStringClaim(AZURE_CLAIM_AZP);
		}
		return jwtTokenClaims.getStringClaim(AZURE_CLAIM_AZP);
	}

	protected List<String> findAzureRoles(JwtToken jwtToken) {
		if (jwtToken.getJwtTokenClaims().getAllClaims().containsKey(AZURE_CLAIM_ROLES)) {
			return jwtToken.getJwtTokenClaims().getAsList(AZURE_CLAIM_ROLES).stream().map(String::toLowerCase).collect(Collectors.toList());
		} else {
			log.error("Azure client credential token flow token mangler roles claim. Permissions.roles må være satt på client i aad-iac. Må undersøkes.");
			return new ArrayList<>();
		}
	}
}
