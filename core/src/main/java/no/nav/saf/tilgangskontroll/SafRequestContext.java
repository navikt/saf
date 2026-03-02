package no.nav.saf.tilgangskontroll;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.nav.security.token.support.core.context.TokenValidationContext;

import static no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode.ORIGINAL;

/// Holder kontekst om kall inn til saf.
@Slf4j
@Getter
public class SafRequestContext {
	public static final String KEY = SafRequestContext.class.getName();
	private final String navCallId;
	private final SafSecurityContext securityContext;
	private final RequestCache requestCache;
	private final boolean originalDokument;

	public SafRequestContext(String navCallId, String navUserId, TokenValidationContext tokenValidationContext, String requestVariantFormat) {
		this.navCallId = navCallId;
		this.securityContext = new SafSecurityContext(tokenValidationContext, navUserId);
		this.requestCache = new RequestCache(securityContext.isSystem());
		this.originalDokument = ORIGINAL.name().equals(requestVariantFormat);
	}

	public SafRequestContext(String navCallId, String navUserId, TokenValidationContext tokenValidationContext) {
		this.navCallId = navCallId;
		this.securityContext = new SafSecurityContext(tokenValidationContext, navUserId);
		this.requestCache = new RequestCache(securityContext.isSystem());
		this.originalDokument = false;
	}

	public boolean isSystemAndVariantformatOriginal() {
		return securityContext.isSystem() && originalDokument;
	}

	/// @return Nav ansatt som har gjort kallet. System hvis det ikke er en Nav ansatt.
	public String getUserId() {
		return securityContext.getUserId();
	}

	/// @return `true` hvis userId er Nav ansatt
	public boolean isUserIdNavAnsatt() {
		return securityContext.isUserIdNavAnsatt();
	}

	/// @return System som har gjort kallet.
	public String getConsumerId() {
		return securityContext.getConsumerId();
	}

	/// @return `true` hvis request er system, `false` hvis Nav ansatt
	public boolean isSystem() {
		return securityContext.isSystem();
	}
}
