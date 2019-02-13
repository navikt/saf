package no.nav.saf.tilgangskontroll;

import graphql.execution.ExecutionId;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.tilgangskontroll.validation.OidcValidatorTool;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Getter
public class SafRequestContext {
	private final SafSecurityContext securityContext;
	private final RequestCache requestCache;
	private final OidcValidatorTool oidcValidatorTool;

	public SafRequestContext(String authorizationHeader, String xCorrelationIDHeader, OidcValidatorTool oidcValidatorTool) {
		this.requestCache = new RequestCache();
		this.oidcValidatorTool = oidcValidatorTool;
		this.securityContext = new SafSecurityContext(authorizationHeader, xCorrelationIDHeader, oidcValidatorTool);
	}

	public void setCorrelationId(ExecutionId executionId) {
		securityContext.useExecutionIDIfXCorrelationIDNull(executionId);
	}
}
