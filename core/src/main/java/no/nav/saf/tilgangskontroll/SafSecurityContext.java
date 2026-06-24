package no.nav.saf.tilgangskontroll;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.exceptions.AuthorizationException;
import no.nav.security.token.support.core.context.TokenValidationContext;
import no.nav.security.token.support.core.jwt.JwtToken;
import no.nav.security.token.support.core.jwt.JwtTokenClaims;

import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toSet;
import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;
import static no.nav.saf.util.MDCUtility.addMdcData;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/// Holder informasjon om token. Opprettes for hvert kall til saf.
@Slf4j
public class SafSecurityContext {

	private static final String ISSUER_AZUREV2 = "azurev2";
	// Azure claims. https://docs.microsoft.com/en-us/azure/active-directory/develop/access-tokens#payload-claims
	static final String AZURE_CLAIM_AZP = "azp";
	static final String AZURE_CLAIM_OID = "oid";
	static final String AZURE_CLAIM_SUB = "sub";
	static final String AZURE_CLAIM_ROLES = "roles";
	// NAV custom Azure claim. https://doc.nais.io/security/auth/azure-ad/configuration/#extra
	static final String AZURE_NAV_CUSTOM_CLAIM_NAVIDENT = "NAVident";
	static final String AZURE_NAV_CUSTOM_CLAIM_AZP_NAME = "azp_name";

	private static final String UKJENT_CONSUMER_ID = "ukjentConsumerId";
	private static final String UKJENT_USER_ID = "ukjentUserId";
	static final String NAVIDENT_REGEX = "^[a-zA-Z]\\d{6}$";
	public static final Pattern NAVIDENT_PATTERN = Pattern.compile(NAVIDENT_REGEX);
	public static final String TEMA_ALLE_ROLE = "tema_alle";
	public static final String TILGANG_NAV_USERID_HEADER_ROLE = "tilgang_nav_user_id_header";
	private static final String JOURNAL_TEMA_ROLE_PREFIX = "journal_tema_";
	private static final String DOKUMENT_TEMA_ROLE_PREFIX = "dokument_tema_";

	private final JwtToken jwtToken;
	@Getter(PRIVATE)
	private final boolean jwtIssuedByAzure;
	///  Om token er utsted av Azure i deres client credential flow.
	@Getter
	private final boolean jwtAzureClientCredentialFlow;
	@Getter(PROTECTED)
	private final boolean userIdNavAnsatt;
	private final Set<String> jwtAzureRoles;
	@Getter(PROTECTED)
	private final String userId;
	@Getter(PROTECTED)
	private final String consumerId;

	SafSecurityContext(TokenValidationContext tokenValidationContext, String navUserIdHeader) {
		this.jwtToken = tokenValidationContext.getFirstValidToken();
		if (this.jwtToken == null) {
			throw new AuthorizationException("Tilgang er avvist. Ingen gyldig token på Authorization header. Token må være utsted av NAV onprem security-token-service eller azure.");
		}

		// Payload fra JWT hentes ut en gang pga den blir hentet ut fra kontekst ofte.
		this.jwtIssuedByAzure = tokenValidationContext.hasTokenFor(ISSUER_AZUREV2);
		this.jwtAzureClientCredentialFlow = isClientCredentialFlowToken(jwtToken);
		if (jwtIssuedByAzure && jwtAzureClientCredentialFlow) {
			this.jwtAzureRoles = findAzureRoles(jwtToken);
		} else {
			this.jwtAzureRoles = Set.of();
		}
		this.userId = mapUserId(navUserIdHeader);
		this.userIdNavAnsatt = NAVIDENT_PATTERN.matcher(this.userId).matches();
		this.consumerId = mapConsumerId();
	}

	/// Om tilgang representerer et system eller en Nav ansatt
	///
	/// @return `true` hvis token representerer et system, ellers `false`
	public boolean isSystem() {
		return isJwtAzureClientCredentialFlow() && !isUserIdNavAnsatt();
	}

	/// Sjekker om konsument har tilgang til journal-tema gjennom rolen `"journal_tema_{tema}"` i roles claim på token. (Azure)
	/// Tema rolen gir tilgang til metadata (kun relevant for tema KTA og FAR)
	/// tema_alle rolen gir tilgang til alle tema.
	/// Se nais/naiserator.yaml azureator config
	///
	/// @param tema Temakode. Eksempel `FOR`
	/// @return true hvis tema rolen finnes. Ellers false
	public boolean hasJournalTilgangEntraRole(Tema tema) {
		return hasEntraRoleOrAlleTemaRole(JOURNAL_TEMA_ROLE_PREFIX, tema);
	}

	/// Sjekker om konsument har tilgang til dokument-tema gjennom rolen `"dokument_tema_{tema}"` i roles claim på token. (Azure)
	/// Tema rolen gir tilgang til dokumenter.
	/// tema_alle rolen gir tilgang til alle tema.
	/// Se nais/naiserator.yaml azureator config
	///
	/// @param tema Temakode. Eksempel `FOR`
	/// @return true hvis tema rolen finnes. Ellers false
	public boolean hasDokumentTilgangEntraRole(Tema tema) {
		return hasEntraRoleOrAlleTemaRole(DOKUMENT_TEMA_ROLE_PREFIX, tema);
	}

	private boolean hasEntraRoleOrAlleTemaRole(String role, Tema tema) {
		if (containsEntraRole(TEMA_ALLE_ROLE)) {
			return true;
		}
		return tema != null && containsEntraRole(role + tema.name().toLowerCase());
	}

	private boolean containsEntraRole(String role) {
		return jwtAzureRoles.contains(role);
	}

	private String mapUserId(String navUserIdHeader) {
		if (navUserIdHeader == null) {
			return getUserIdFromToken();
		} else if (isNotBlank(navUserIdHeader) && isClientCredentialFlowToken(jwtToken)) {
			if (navIdentHarFeilFormat(navUserIdHeader)) {
				log.error("Tjeneste kalt med Nav-User-Id header og maskin-til-maskin Entra token. Ugyldig format på NAVIdent={}. Må matche \"^[a-zA-Z]\\d{6}$\". " +
						"Konsument må informeres og bes om å rette dette. Maskinens tilganger vil bli benyttet for tilgangskontroll.", navUserIdHeader);
				return getUserIdFromToken();
			}

			if (rollenTilgangNavUserIdHeaderMangler()) {
				log.warn("Tjeneste kalt med Nav-User-Id header og maskin-til-maskin Entra token. Konsument har ikke role={}.", TILGANG_NAV_USERID_HEADER_ROLE);
				throw new AuthorizationException("Tilgang er avvist. Tjeneste kalt med Nav-User-Id header og maskin-til-maskin Entra token krever role=%s.".formatted(TILGANG_NAV_USERID_HEADER_ROLE));
			}
			return navUserIdHeader;
		}
		return getUserIdFromToken();
	}

	private String mapConsumerId() {
		if (isJwtAzureClientCredentialFlow() || isOnBehalfOfFlowToken()) {
			return findAzureAppnameClaim(jwtToken.getJwtTokenClaims());
		}
		return UKJENT_CONSUMER_ID;
	}

	private String getUserIdFromToken() {
		if (isClientCredentialFlowToken(jwtToken)) {
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

	private boolean isOnBehalfOfFlowToken() {
		final JwtTokenClaims jwtTokenClaims = jwtToken.getJwtTokenClaims();
		return jwtTokenClaims.getStringClaim(AZURE_CLAIM_SUB) != null &&
				jwtTokenClaims.getStringClaim(AZURE_CLAIM_OID) != null &&
				!jwtTokenClaims.getStringClaim(AZURE_CLAIM_SUB).equals(jwtTokenClaims.getStringClaim(AZURE_CLAIM_OID));
	}

	private boolean isClientCredentialFlowToken(JwtToken jwtToken) {
		if (isJwtIssuedByAzure()) {
			final JwtTokenClaims jwtTokenClaims = jwtToken.getJwtTokenClaims();
			return jwtTokenClaims.getStringClaim(AZURE_CLAIM_SUB) != null &&
					jwtTokenClaims.getStringClaim(AZURE_CLAIM_OID) != null &&
					jwtTokenClaims.getStringClaim(AZURE_CLAIM_SUB).equals(jwtTokenClaims.getStringClaim(AZURE_CLAIM_OID));
		} else {
			return false;
		}
	}

	private String findAzureAppnameClaim(JwtTokenClaims jwtTokenClaims) {
		if (jwtTokenClaims.getAllClaims().containsKey(AZURE_NAV_CUSTOM_CLAIM_AZP_NAME)) {
			String azpnameClaim = jwtTokenClaims.getStringClaim(AZURE_NAV_CUSTOM_CLAIM_AZP_NAME);
			if (isNotBlank(azpnameClaim)) {
				return azpnameClaim;
			}
			return jwtTokenClaims.getStringClaim(AZURE_CLAIM_AZP);
		}
		return jwtTokenClaims.getStringClaim(AZURE_CLAIM_AZP);
	}

	private Set<String> findAzureRoles(JwtToken jwtToken) {
		if (jwtToken.getJwtTokenClaims().getAllClaims().containsKey(AZURE_CLAIM_ROLES)) {
			return jwtToken.getJwtTokenClaims().getAsList(AZURE_CLAIM_ROLES).stream()
					.map(String::toLowerCase)
					.collect(toSet());
		} else {
			addMdcData(UUID.randomUUID().toString(), getUserId(), getConsumerId());
			log.error("Azure client credential token flow token mangler roles claim. Permissions.roles må være satt på client i azureator. Må undersøkes.");
			return Set.of();
		}
	}

	private static boolean navIdentHarFeilFormat(String navUserIdHeader) {
		return !NAVIDENT_PATTERN.matcher(navUserIdHeader).matches();
	}

	private boolean rollenTilgangNavUserIdHeaderMangler() {
		return !jwtAzureRoles.contains(TILGANG_NAV_USERID_HEADER_ROLE);
	}

}