package no.nav.saf.tilgangskontroll.pep;

import io.lettuce.core.RedisException;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.nav.entraproxy.EntraProxyConsumer;
import no.nav.saf.anticorruptionlayer.nav.entraproxy.EntraProxyTematilgangResponse;
import no.nav.saf.cache.KeyGeneratorDistributedCaching;
import no.nav.saf.cache.KeyGeneratorLocalCaching;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Advice;
import no.nav.saf.tilgangskontroll.abac.dto.response.AttributeAssignment;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import no.nav.saf.tilgangskontroll.pep.reasons.GeografiReason;
import no.nav.saf.tilgangskontroll.pep.reasons.TemaReason;
import no.nav.saf.tilgangskontroll.pep.reasons.UkjentEllerTekniskReason;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.PoolException;
import org.springframework.stereotype.Component;

import java.util.List;

import static no.nav.saf.cache.ValkeyCacheConfig.VALKEY_CACHE_MANAGER;
import static no.nav.saf.cache.ValkeyCacheConfig.VALKEY_DOKUMENT_TILGANG_CACHE;
import static no.nav.saf.domain.DomainConstants.PEP2D;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_TEMA;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_SAK_DOKUMENT;
import static no.nav.saf.tilgangskontroll.abac.dto.response.Decision.DENY;
import static no.nav.saf.tilgangskontroll.abac.service.advice.AdviceTypes.DENY_REASON;
import static no.nav.saf.tilgangskontroll.pep.PepAnswer.permit;

/**
 * Dekker følgende policies i saf:
 * <p>
 * https://confluence.adeo.no/pages/viewpage.action?pageId=313329243
 * <p>
 * Lokal caching er kun relevant for dokumentoversiktene og brukes i journalpostMapperDto.java
 */
@Slf4j
@Component(PEP2D)
public class AbacBackedPep2dImpl extends StandardAbacBackedPep<TilgangSak> {

	private final boolean featureToggleEntraProxy;
	private final Cache tilgangCache;
	private final AbacService abacService;
	private final EntraProxyConsumer entraProxyConsumer;

	@Autowired
	public AbacBackedPep2dImpl(@Value("${saf.pep2.feature_toggle_entra_proxy}") boolean featureToggleEntraProxy,
							   @Qualifier(VALKEY_CACHE_MANAGER) CacheManager redisCacheManager,
							   AbacService abacService,
							   EntraProxyConsumer entraProxyConsumer) {
		this.featureToggleEntraProxy = featureToggleEntraProxy;
		this.tilgangCache = redisCacheManager.getCache(VALKEY_DOKUMENT_TILGANG_CACHE);
		this.abacService = abacService;
		this.entraProxyConsumer = entraProxyConsumer;
	}

	@Override
	public PepAnswer verifyAbacPdpDecision(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.info("Pep2d(tema-tilgang) mangler data om sak. Tilgang gis likevel for at saksbehandler skal kunne knytte dokument til sak og bruker.");
			return PepAnswer.permit();
		}

		traceLogPepStarted(PEP2D, ressurs);
		Tema tema = ressurs.getTema();
		String tilgangKeyDistributedCaching = KeyGeneratorDistributedCaching.getKeyForPep2d(safRequestContext.getUserId(), tema);
		String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep2d(tema);

		PepAnswer pepAnswer;
		// Try-catch er fordi redis ikke fungerer lokalt
		try {
			if (featureToggleEntraProxy) {
				pepAnswer = getPepAnswerFromEntraProxy(ressurs, safRequestContext, tilgangKeyDistributedCaching);
			} else {
				pepAnswer = fetchXacmlResponse(ressurs, safRequestContext, tilgangKeyDistributedCaching);
			}

			safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, pepAnswer);
			return pepAnswer;
		} catch (RedisSystemException | RedisException | PoolException | Cache.ValueRetrievalException | RedisConnectionFailureException _) {
			// Ting skal fremdeles snurre selv om man ikke får kontakt med redis
			if (featureToggleEntraProxy) {
				pepAnswer = getPepAnswerFromEntraProxy(ressurs, safRequestContext, null);
			} else {
				XacmlResponse response = hasDokumentAccess(ressurs, safRequestContext);
				pepAnswer = mapToAbacAnswer(response, tema);
			}

			safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, pepAnswer);
			return pepAnswer;
		} finally {
			traceLogPepFinished(PEP2D, ressurs);
		}
	}

	@Override
	protected PepAnswer translateToDenyReasonCode(XacmlResponse xacmlResponse) {
		throw new UnsupportedOperationException("Translating from DenyReasonCode is not supported in PEP2");
	}

	@Override
	public PepAnswer verifyAzureClientCredentialFlowAccess(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.info("Pep2d(tema-tilgang) mangler data om sak. Tilgang gis likevel for at system skal kunne knytte dokument til sak og bruker. Azure ccf.");
			return permit();
		}
		traceLogPepStarted(PEP2D, ressurs);
		Tema tema = ressurs.getTema();
		String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep2d(tema);
		boolean decision = safRequestContext.isSystemAndVariantformatOriginal() || safRequestContext.getSecurityContext().hasDokumentTilgangEntraRole(tema);
		PepAnswer pepAnswer = decision ? permit() : PepAnswer.deny(new TemaReason(
				"cause_0013_ikketilgangtilDokumenttema", "saf_pep2d", "mangler_tema", tema));
		safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, pepAnswer);
		traceLogPepFinished(PEP2D, ressurs);
		return pepAnswer;
	}

	@Override
	PepAnswer verifyRestSTSCredentialFlowAccess(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (safRequestContext.isUserIdNavAnsatt()) {
			return verifyAbacPdpDecision(ressurs, safRequestContext);
		}

		log.warn("Rest-STS servicebruker forsøker å hente dokument uten at Nav-User-Id header er satt. Tilgang nektes for tema={}", ressurs.getTema());
		return PepAnswer.deny(new UkjentEllerTekniskReason());
	}

	protected PepAnswer mapToAbacAnswer(XacmlResponse xacmlResponse, Tema tema) {
		if (xacmlResponse.isPermit()) {
			return PepAnswer.permit();
		} else {
			var advices = xacmlResponse.getAdvicesMap();
			if (AbacDenyReasonCode.GEOGRAFI.matchesAbacAdvice(advices)) {
				return PepAnswer.deny(new GeografiReason(advices));
			}
			return PepAnswer.deny(new TemaReason(advices, tema));
		}
	}

	private PepAnswer fetchXacmlResponse(TilgangSak ressurs, SafRequestContext safRequestContext, String tilgangKeyDistributedCaching) {
		XacmlResponse cachedResponse = tilgangCache.get(tilgangKeyDistributedCaching, XacmlResponse.class);
		if (cachedResponse == null) {
			XacmlResponse abacResponse = hasDokumentAccess(ressurs, safRequestContext);
			if (abacResponse == null) {
				log.warn("Pep2d mangler data for å kunne gjennomføre tilgangskontroll. Tomt svar fra ABAC. tema={}", ressurs.getTema());
				return PepAnswer.deny(new UkjentEllerTekniskReason());
			}
			if (abacResponse.isPermit()) {
				tilgangCache.put(tilgangKeyDistributedCaching, abacResponse);
			}
			return mapToAbacAnswer(abacResponse, ressurs.getTema());
		} else {
			return mapToAbacAnswer(cachedResponse, ressurs.getTema());
		}
	}

	private XacmlResponse hasDokumentAccess(TilgangSak ressurs, SafRequestContext safRequestContext) {
		XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext());
		request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_SAK_DOKUMENT);
		request.resource(RESOURCE_FELLES_TEMA, ressurs.getTema().name());
		return abacService.evaluate(request);
	}

	private PepAnswer getPepAnswerFromEntraProxy(TilgangSak ressurs, SafRequestContext safRequestContext, String tilgangKeyDistributedCaching) {
		XacmlResponse cachedResponse = getFromCache(tilgangKeyDistributedCaching);
		if (cachedResponse != null) {
			return mapToAbacAnswer(cachedResponse, ressurs.getTema());
		}

		try {
			EntraProxyTematilgangResponse response = entraProxyConsumer.hentTematilgangForNavAnsatt(safRequestContext);
			Tema tema = ressurs.getTema();

			if (response.harTilgangTilTema(tema)) {
				updateCache(tilgangKeyDistributedCaching, XacmlResponse.permit());
				return PepAnswer.permit();
			}

			TemaReason temaReason = new TemaReason("cause_0013_ikketilgangtilDokumenttema", "saf_pep2d", "tematilgang_nok", tema);
			return PepAnswer.deny(temaReason);

		} catch (Exception e) {
			log.error("Pep2d (tematilgang): Kall mot Entra-proxy feilet, fallback til abac-saf.", e);
			return fetchXacmlResponse(ressurs, safRequestContext, tilgangKeyDistributedCaching);
		}
	}

	private XacmlResponse getFromCache(String key) {
		return key != null ? tilgangCache.get(key, XacmlResponse.class) : null;
	}

	private void updateCache(String key, XacmlResponse response) {
		if (key != null) {
			tilgangCache.put(key, response);
		}
	}
}
