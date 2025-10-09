package no.nav.saf.tilgangskontroll;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.security.token.support.core.context.TokenValidationContext;

import java.util.Map;

import static no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode.ORIGINAL;

/**
 * Holder kontekst om kall inn til saf.
 */
@Slf4j
@Getter
public class SafRequestContext {
	public static final String KEY = SafRequestContext.class.getName();
	private final String navCallId;
	private final SafSecurityContext securityContext;
	private final RequestCache requestCache;
	private final boolean originalDokument;

	public SafRequestContext(String navCallId, String navUserId, TokenValidationContext tokenValidationContext, Map<String, Boolean> privilegiedServiceusers, VariantFormatCode requestVariantFormat) {
		this.navCallId = navCallId;
		this.securityContext = new SafSecurityContext(tokenValidationContext, privilegiedServiceusers, navUserId);
		this.requestCache = new RequestCache(securityContext.isSystem());
		this.originalDokument = requestVariantFormat == ORIGINAL;
	}

	public SafRequestContext(String navCallId, String navUserId, TokenValidationContext tokenValidationContext, Map<String, Boolean> privilegiedServiceusers) {
		this.navCallId = navCallId;
		this.securityContext = new SafSecurityContext(tokenValidationContext, privilegiedServiceusers, navUserId);
		this.requestCache = new RequestCache(securityContext.isSystem());
		this.originalDokument = false;
	}

	public boolean isSystemAndVariantformatOriginal() {
		return securityContext.isSystem() && originalDokument;
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

	public boolean isUserIdNavAnsatt() {
		return securityContext.isUserIdNavAnsatt();
	}

	/**
	 * Om token i request er i kontekst av system eller bruker.
	 *
	 * @return true hvis token er utsted av REST-STS eller er Azure client-credential flow, ellers false
	 */
	public boolean isSystem() {
		return securityContext.isSystem();
	}
}
