package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_TEMA;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_SAK_DOKUMENT;
import static no.nav.saf.cache.RedisCacheConfig.TILGANG_CACHE;
import static no.nav.saf.domain.DomainConstants.PEP2D;

import io.lettuce.core.RedisException;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.cache.KeyGeneratorDistributedCaching;
import no.nav.saf.cache.KeyGeneratorLocalCaching;
import no.nav.saf.cache.RedisCacheConfig;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.PoolException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Dekker følgende policies i saf:
 * <p>
 * https://confluence.adeo.no/pages/viewpage.action?pageId=305352853
 * <p>
 * Lokal caching er kun relevant for dokumentoversiktene og brukes i journalpostMapperDto.java
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component(PEP2D)
public class Pep2dImpl implements Pep<TilgangSak> {

	private final Cache tilgangCache;
	private final AbacService abacService;

	@Inject
	public Pep2dImpl(@Named(RedisCacheConfig.MANAGER_DISTRIBUTED) CacheManager redisCacheManager, AbacService abacService) {
		this.tilgangCache = redisCacheManager.getCache(TILGANG_CACHE);
		this.abacService = abacService;
	}

	@Override
	public XacmlResponse verifyAccessXacmlResponse(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null || ressurs.getTema() == null) {
			log.info("Pep2d mangler data om sak. Tilgang gis likevel for at saksbehandler skal kunne knytte dokument til sak og bruker.");
			return XacmlResponse.permit();
		}

		Pep.traceLogPepStarted(PEP2D, ressurs);
		String tilgangKeyDistributedCaching = KeyGeneratorDistributedCaching.getKeyForPep2d(safRequestContext.getSecurityContext()
				.getSubjectId(), ressurs.getTema().name());
		String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep2d(ressurs.getTema().name());
		// Ty-catch er fordi redis ikke fungerer lokalt
		try {
			XacmlResponse response = fetchXacmlResponse(ressurs, safRequestContext, tilgangKeyDistributedCaching);
			if (response == null) {
				return XacmlResponse.deny();
			}
			safRequestContext.getRequestCache().putObject(tilgangKeyLocalCaching, decide(response.getDecision()));
			return response;
		} catch (RedisSystemException | RedisException | PoolException | Cache.ValueRetrievalException e) {
			// Ting skal fremdeles snurre selv om man ikke får kontakt med redis
			XacmlResponse response = hasDokumentAccess(ressurs, safRequestContext);
			safRequestContext.getRequestCache().putObject(tilgangKeyLocalCaching, decide(response.getDecision()));
			return response;
		} finally {
			Pep.traceLogPepFinished(PEP2D, ressurs);
		}
	}

	private XacmlResponse fetchXacmlResponse(TilgangSak ressurs, SafRequestContext safRequestContext, String tilgangKeyDistributedCaching) {
		XacmlResponse cachedResponse = tilgangCache.get(tilgangKeyDistributedCaching, XacmlResponse.class);
		if (cachedResponse == null) {
			XacmlResponse abacResponse = hasDokumentAccess(ressurs, safRequestContext);
			if (abacResponse == null) {
				return XacmlResponse.deny();
			}
			if (decide(abacResponse.getDecision())) {
				tilgangCache.put(tilgangKeyDistributedCaching, abacResponse);
			}
			return abacResponse;
		} else {
			return cachedResponse;
		}
	}

	private boolean decide(Decision decision) {
		return Decision.PERMIT.equals(decision);
	}

	private XacmlResponse hasDokumentAccess(TilgangSak ressurs, SafRequestContext safRequestContext) {
		XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext());
		request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_SAK_DOKUMENT);
		request.resource(RESOURCE_FELLES_TEMA, ressurs.getTema().name());
		return abacService.evaluate(request);
	}
}
