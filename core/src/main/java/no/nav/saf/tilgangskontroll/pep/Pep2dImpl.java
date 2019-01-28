package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_SAK_DOKUMENT;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_TEMA;
import static no.nav.saf.cache.RedisCacheConfig.TILGANG_CACHE;

import io.lettuce.core.RedisException;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.cache.RedisCacheConfig;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.PoolException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Dekker følgende policies i saf:
 * <p>
 * https://confluence.adeo.no/pages/viewpage.action?pageId=305352853
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component("pep2d")
public class Pep2dImpl implements Pep<TilgangSak> {

	private final Cache tilgangCache;
	private final AbacService abacService;

	@Inject
	public Pep2dImpl(@Named(RedisCacheConfig.MANAGER_DISTRIBUTED) CacheManager redisCacheManager, AbacService abacService) {
		this.tilgangCache = redisCacheManager.getCache(TILGANG_CACHE);
		this.abacService = abacService;
	}

	@Override
	public boolean hasAccess(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.warn("Pep2d mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll");
			return false;
		}

		if (ressurs.getTema() != null) {
			Pep.traceLogPepStarted("pep2d", ressurs);

			String tilgangKey = "tilgang:" + safRequestContext.getSecurityContext()
					.getSaksbehandlerId() + ":tema=" + ressurs.getTema();
			try {
				boolean decide = decide(tilgangCache.get(tilgangKey,
						() -> hasDokumentAccess(ressurs, safRequestContext)));
				safRequestContext.getRequestCache().putObject(tilgangKey, decide);
				return decide;
			} catch (RedisException | PoolException | Cache.ValueRetrievalException e) {
				boolean decide = decide(hasDokumentAccess(ressurs, safRequestContext));
				safRequestContext.getRequestCache().putObject(tilgangKey, decide);
				return decide;
			} finally {
				Pep.traceLogPepFinished("pep2d", ressurs);
			}
		} else {
			return true;
		}
	}

	private boolean decide(Decision decision) {
		return Decision.PERMIT.equals(decision);
	}

	private Decision hasDokumentAccess(TilgangSak ressurs, SafRequestContext safRequestContext) {
		XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext().getOidcTokenBody());
		request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_SAK_DOKUMENT);
		request.resource(RESOURCE_SAF_TEMA, ressurs.getTema().name());
		XacmlResponse response = abacService.evaluate(request);
		return response.getDecision();
	}
}
