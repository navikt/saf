package no.nav.saf.tilgangskontroll;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Getter
public class SafRequestContext {
	private final SafSecurityContext securityContext;
	private final RequestCache requestCache;

	public SafRequestContext(String authorizationHeader) {
		this.securityContext = new SafSecurityContext(authorizationHeader);
		this.requestCache = new RequestCache();
	}
}
