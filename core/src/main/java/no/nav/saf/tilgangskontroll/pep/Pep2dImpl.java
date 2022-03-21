package no.nav.saf.tilgangskontroll.pep;

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
import no.nav.saf.tilgangskontroll.pep.AbacAnswer.AbacDenyReason;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.PoolException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;

import static no.nav.saf.cache.RedisCacheConfig.TILGANG_CACHE;
import static no.nav.saf.domain.DomainConstants.PEP2D;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_TEMA;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_SAK_DOKUMENT;
import static no.nav.saf.tilgangskontroll.pep.AbacAnswer.deny;
import static no.nav.saf.tilgangskontroll.pep.AbacAnswer.permit;

/**
 * Dekker følgende policies i saf:
 * <p>
 * https://confluence.adeo.no/pages/viewpage.action?pageId=313329243
 * <p>
 * Lokal caching er kun relevant for dokumentoversiktene og brukes i journalpostMapperDto.java
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component(PEP2D)
public class Pep2dImpl extends Pep<TilgangSak> {

	private final Cache tilgangCache;
	private final AbacService abacService;

	@Inject
	public Pep2dImpl(@Named(RedisCacheConfig.MANAGER_DISTRIBUTED) CacheManager redisCacheManager, AbacService abacService) {
		this.tilgangCache = redisCacheManager.getCache(TILGANG_CACHE);
		this.abacService = abacService;
	}

	@Override
	public XacmlResponse verifyAbacPdpDecision(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null || ressurs.getTema() == null) {
			log.info("Pep2d(tema-tilgang) mangler data om sak. Tilgang gis likevel for at saksbehandler skal kunne knytte dokument til sak og bruker.");
			return XacmlResponse.permit();
		}

		traceLogPepStarted(PEP2D, ressurs);
		String tilgangKeyDistributedCaching = KeyGeneratorDistributedCaching.getKeyForPep2d(safRequestContext.getUserId(), ressurs.getTema().name());
		String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep2d(ressurs.getTema().name());
		// Try-catch er fordi redis ikke fungerer lokalt
		try {
			XacmlResponse response = fetchXacmlResponse(ressurs, safRequestContext, tilgangKeyDistributedCaching);
			if (response == null) {
				return XacmlResponse.deny();
			}
			safRequestContext.getRequestCache().putObject(tilgangKeyLocalCaching, decide(response.getDecision()));
			return response;
		} catch (RedisSystemException | RedisException | PoolException | Cache.ValueRetrievalException | RedisConnectionFailureException e) {
			// Ting skal fremdeles snurre selv om man ikke får kontakt med redis
			XacmlResponse response = hasDokumentAccess(ressurs, safRequestContext);
			safRequestContext.getRequestCache().putObject(tilgangKeyLocalCaching, decide(response.getDecision()));
			return response;
		} finally {
			traceLogPepFinished(PEP2D, ressurs);
		}
	}

	@Override
	public AbacAnswer verifyAzureClientCredentialFlowAccess(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null || ressurs.getTema() == null) {
			log.info("Pep2d(tema-tilgang) mangler data om sak. Tilgang gis likevel for at system skal kunne knytte dokument til sak og bruker. Azure ccf.");
			return permit();
		}
		traceLogPepStarted(PEP2D, ressurs);
		String temakode = ressurs.getTema().name();
		String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep2d(temakode);
		boolean decision = safRequestContext.getSecurityContext().hasTemaAureRole(temakode.toLowerCase());
		safRequestContext.getRequestCache().putObject(tilgangKeyLocalCaching, decision);
		traceLogPepFinished(PEP2D, ressurs);
		return decision ? permit() : deny(AbacDenyReason.builder()
				.cause("ingen_tema_tilgang").policy("saf_pep2d").rule("clientid_mangler_tema_role")
				.build());
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
