package no.nav.saf.tilgangskontroll.pep;

import io.lettuce.core.RedisException;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.cache.KeyGeneratorDistributedCaching;
import no.nav.saf.cache.KeyGeneratorLocalCaching;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import no.nav.saf.tilgangskontroll.pep.reasons.GeografiReason;
import no.nav.saf.tilgangskontroll.pep.reasons.TemaReason;
import no.nav.saf.tilgangskontroll.pep.reasons.UkjentEllerTekniskReason;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.PoolException;
import org.springframework.stereotype.Component;

import static no.nav.saf.cache.ValkeyCacheConfig.VALKEY_CACHE_MANAGER;
import static no.nav.saf.cache.ValkeyCacheConfig.VALKEY_DOKUMENT_TILGANG_CACHE;
import static no.nav.saf.domain.DomainConstants.PEP2D;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_TEMA;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_SAK_DOKUMENT;
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
public class AbacBackedPep2dImpl extends Pep<TilgangSak> {

	private final Cache tilgangCache;
	private final AbacService abacService;

	@Autowired
	public AbacBackedPep2dImpl(@Qualifier(VALKEY_CACHE_MANAGER) CacheManager redisCacheManager, AbacService abacService) {
		this.tilgangCache = redisCacheManager.getCache(VALKEY_DOKUMENT_TILGANG_CACHE);
		this.abacService = abacService;
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
		// Try-catch er fordi redis ikke fungerer lokalt
		try {
			PepAnswer pepAnswer = fetchXacmlResponse(ressurs, safRequestContext, tilgangKeyDistributedCaching);
			safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, pepAnswer);
			return pepAnswer;
		} catch (RedisSystemException | RedisException | PoolException | Cache.ValueRetrievalException | RedisConnectionFailureException e) {
			// Ting skal fremdeles snurre selv om man ikke får kontakt med redis
			XacmlResponse response = hasDokumentAccess(ressurs, safRequestContext);
			PepAnswer pepAnswer = mapToAbacAnswer(response, tema);
			safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, pepAnswer);
			return pepAnswer;
		} finally {
			traceLogPepFinished(PEP2D, ressurs);
		}
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
		boolean decision = safRequestContext.getSecurityContext().hasTemaAzureRole(tema);
		PepAnswer pepAnswer = decision ? permit() : PepAnswer.deny(new TemaReason(
				"cause_0013_ikketilgangtiltema", "saf_pep2d", "mangler_tema", tema));
		safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, pepAnswer);
		traceLogPepFinished(PEP2D, ressurs);
		return pepAnswer;
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
}
