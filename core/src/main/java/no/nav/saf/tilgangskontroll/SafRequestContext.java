package no.nav.saf.tilgangskontroll;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.context.TokenValidationContext;

import java.util.Map;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Getter
public class SafRequestContext {
	public static final String KEY = SafRequestContext.class.getName();
	private final String navCallId;
	private final SafSecurityContext securityContext;
	private final RequestCache requestCache;

	public SafRequestContext(String navCallId, TokenValidationContext tokenValidationContext, Map<String, Boolean> privilegiedServiceusers) {
		this.requestCache = new RequestCache();
		this.navCallId = navCallId;
		this.securityContext = new SafSecurityContext(tokenValidationContext, privilegiedServiceusers);
	}

	/**
	 * @return System som har gjort kallet.
	 */
	public String getConsumerId() {
		return securityContext.getConsumerId();
	}

	/**
	 * @return Bruker som har gjort kallet. System hvis det ikke er en saksbehandler.
	 */
	public String getUserId() {
		return securityContext.getUserId();
	}
}
