package no.nav.saf.tilgangskontroll;

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

	public SafRequestContext(String authorizationHeader, OidcValidatorTool oidcValidatorTool) {
		this.securityContext = new SafSecurityContext(authorizationHeader, oidcValidatorTool);
		this.requestCache = new RequestCache();
		this.oidcValidatorTool = oidcValidatorTool;
	}
}
