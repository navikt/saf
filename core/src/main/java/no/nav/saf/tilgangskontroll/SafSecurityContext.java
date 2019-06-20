package no.nav.saf.tilgangskontroll;

import static no.nav.saf.util.MDCUtility.addMdcData;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trim;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import graphql.execution.ExecutionId;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.exceptions.OidcAuthorizationException;
import no.nav.saf.tilgangskontroll.validation.OidcValidatorTool;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
public class SafSecurityContext {
	private static final String OIDC_TOKEN_PREFIX = "Bearer ";
	private final String oidcTokenBody;
	private final String saksbehandlerId;
	private String xCorrelationID;
	private final OidcValidatorTool oidcValidatorTool;

	SafSecurityContext(String authorizationHeader,
					   String xCorrelationIDHeader,
					   OidcValidatorTool oidcValidatorTool) {

		this.saksbehandlerId = getSubjectFromToken(authorizationHeader);
		this.oidcValidatorTool = oidcValidatorTool;
		this.oidcTokenBody = getOidcTokenBody(authorizationHeader);
		// if zero, then executionId from graphQl is used.
		this.xCorrelationID = trim(xCorrelationIDHeader);

		addMdcData(this.saksbehandlerId, this.xCorrelationID);
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

	public void useExecutionIDIfXCorrelationIDNull(ExecutionId executionId) {
		if (isBlank(xCorrelationID)) {
			this.xCorrelationID = executionId.toString();
			addMdcData(this.saksbehandlerId, this.xCorrelationID);
		}
	}

	public String getXCorrelationID() {
		return xCorrelationID;
	}

	public String getOidcTokenBody() {
		if (oidcTokenBody == null) {
			throw new OidcAuthorizationException("Autentiseringsmekanisme er ikke støttet. " +
					"Kun OIDC-token (JWT via OAuth 2.0) med header \"Authorization\" : \"Bearer {token}\" er tillatt.");
		} else {
			return oidcTokenBody;
		}
	}

	public String getSaksbehandlerId() {
		if (saksbehandlerId == null) {
			throw new OidcAuthorizationException("Autentiseringsmekanisme er ikke støttet. " +
					"Kun OIDC-token (JWT via OAuth 2.0) med header \"Authorization\" : \"Bearer {token}\" er tillatt.");
		} else {
			return saksbehandlerId;
		}
	}

	private boolean isNotValidAuthorizationHeader(String authorizationHeader) {
		return authorizationHeader == null || !authorizationHeader.startsWith(OIDC_TOKEN_PREFIX);
	}
}
