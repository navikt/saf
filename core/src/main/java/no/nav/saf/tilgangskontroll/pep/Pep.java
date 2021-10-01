package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Policy Enforcement Point for ABAC.
 * <p>
 * Evaluerer tilgang til en ressurs T.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface Pep<T> {
	Logger logger = LoggerFactory.getLogger(Pep.class);

	/**
	 * Kall mot abac-saf (ekstern tjeneste) som er Policy Decision Point (PDP).
	 * Bestemmer om kall skal få tilgang til ressurs.
	 * Implementerer:
	 * https://confluence.adeo.no/display/BOA/saf+-+Tilgangskontroll#safTilgangskontroll-TilgangsreglerforNAV-ansatte
	 * https://confluence.adeo.no/display/BOA/saf+-+Tilgangskontroll#safTilgangskontroll-Tilgangsreglerforservicebruker
	 *
	 * @param ressurs           Ressursen som skal sjekkes
	 * @param safRequestContext Kontekst for kallet
	 * @return XacmlResponse med decision. Hvis decision er PERMIT, returner true. Ellers false.
	 */
	XacmlResponse verifyAccessXacmlResponse(T ressurs, SafRequestContext safRequestContext);

	/**
	 * Sjekker tilgang for app registration autentisert med client credential flow i Azure.
	 * Implementerer:
	 * https://confluence.adeo.no/display/BOA/saf+-+Tilgangskontroll#safTilgangskontroll-Tilgangsreglerforservicebrukerautentisertmedclient-credential-flowiAzure
	 *
	 * @param ressurs           Ressursen som skal sjekkes
	 * @param safRequestContext Kontekst for kallet
	 * @return true hvis tilgang til ressurs. Ellers false.
	 */
	boolean verifyAzureClientCredentialFlowAccess(T ressurs, SafRequestContext safRequestContext);

	default boolean hasAccess(T ressurs, SafRequestContext safRequestContext) {
		if (safRequestContext.getSecurityContext().isJwtAzureClientCredentialFlow()) {
			return verifyAzureClientCredentialFlowAccess(ressurs, safRequestContext);
		} else {
			XacmlResponse response = verifyAccessXacmlResponse(ressurs, safRequestContext);
			return Decision.PERMIT.equals(response.getDecision());
		}
	}

	static void traceLogPepStarted(String pepName, Object ressurs) {
		if (logger.isTraceEnabled()) {
			logger.trace("{} evaluerer ressurs={}", pepName, ressurs);
		}
	}

	static void traceLogPepFinished(String pepName, Object ressurs) {
		if (logger.isTraceEnabled()) {
			logger.trace("{} ferdig evaluert ressurs={}", pepName, ressurs);
		}
	}

}
