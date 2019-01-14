package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_PERSON_FNR;
import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_PERSON;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_TEMA;
import static no.nav.saf.cache.RedisCacheConfig.TILGANG_CACHE;
import static no.nav.saf.tilgangskontroll.pep.PepUtils.populateFellesAttributes;

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
			if (log.isTraceEnabled()) {
				log.trace("Pep2d evaluerer arkivsak={}, arkivsaksystem={}, tema={}", ressurs.getArkivsaksnummer(), ressurs.getArkivsaksystem(), ressurs
						.getTema());
			}

			String tilgangKey = "tilgang:" + safRequestContext.getSecurityContext()
					.getSaksbehandlerId() + ":tema=" + ressurs.getTema();
			try {
				boolean decide = decide(
						tilgangCache.get(tilgangKey,
								() -> callPep2d(ressurs, safRequestContext))
				);
				safRequestContext.getRequestCache().putObject(tilgangKey, decide);
				return decide;
			} catch (RedisException | PoolException | Cache.ValueRetrievalException e) {
				boolean decide = decide(callPep2d(ressurs, safRequestContext));
				safRequestContext.getRequestCache().putObject(tilgangKey, decide);
				return decide;
			}
		} else {
			return true;
		}
	}

	private boolean decide(Decision decision) {
		return Decision.PERMIT.equals(decision);
	}

	private Decision callPep2d(TilgangSak ressurs, SafRequestContext safRequestContext) {
		Decision decisionTematilgangMedGeografi = callPep2dTematilgangMedGeografi(ressurs, safRequestContext);
		if (decisionTematilgangMedGeografi.equals(Decision.DENY)) {
			//Ingen grunn til å kall pep GeografiskTilgang dersom pep TematilgangMedGeograf gir Deny
			return Decision.DENY;
		}

		return callPep2GeografiskTilgang(ressurs, safRequestContext);
	}

	private Decision callPep2dTematilgangMedGeografi(TilgangSak ressurs, SafRequestContext safRequestContext) {
		XacmlRequest request = new XacmlRequest();
		populateFellesAttributes(request, safRequestContext.getSecurityContext().getOidcTokenBody());
		request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_TEMA);
		request.resource(RESOURCE_SAF_TEMA, ressurs.getTema());

		XacmlResponse response = abacService.evaluate(request);
		if (log.isTraceEnabled()) {
			log.trace("Pep2dTematilgangMedGeografi ferdig evaluert arkivsak={}, arkivsaksystem={}, tema={}", ressurs.getArkivsaksnummer(), ressurs
					.getArkivsaksystem(), ressurs.getTema());
		}
		return response.getDecision();
	}

	private Decision callPep2GeografiskTilgang(TilgangSak ressurs, SafRequestContext safRequestContext) {
		XacmlRequest request = new XacmlRequest();
		populateFellesAttributes(request, safRequestContext.getSecurityContext().getOidcTokenBody());
		request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_PERSON);
		if (ressurs.getAktoerId() != null) {
			request.resource(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, ressurs.getAktoerId());
		} else if (ressurs.getFoedselsnummer() != null) {
			request.resource(RESOURCE_FELLES_PERSON_FNR, ressurs.getFoedselsnummer());
		} else {
			//Gjør ikke sjekk for geografisk tilgang for organisasjon
			return Decision.PERMIT;
		}

		XacmlResponse response = abacService.evaluate(request);
		if (log.isTraceEnabled()) {
			log.trace("Pep2GeografiskTilgang ferdig evaluert arkivsak={}, arkivsaksystem={}, tema={}", ressurs.getArkivsaksnummer(), ressurs
					.getArkivsaksystem(), ressurs.getTema());
		}
		return response.getDecision();
	}
}
