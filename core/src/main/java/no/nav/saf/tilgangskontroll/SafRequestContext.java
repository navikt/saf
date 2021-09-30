package no.nav.saf.tilgangskontroll;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.context.TokenValidationContext;

import java.util.Map;
import java.util.Set;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Getter
public class SafRequestContext {
	private final SafSecurityContext securityContext;
	private final RequestCache requestCache;

	public SafRequestContext(Set<String> azureIssuers, String navCallid, String navConsumerId, TokenValidationContext tokenValidationContext, Map<String, Boolean> privilegiedServiceusers) {
		this.requestCache = new RequestCache();
		this.securityContext = new SafSecurityContext(azureIssuers, navCallid, navConsumerId, tokenValidationContext, privilegiedServiceusers);
	}
}
