package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_DOKUMENT_FIL;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_SKJERMING;
import static no.nav.saf.cache.RedisCacheConfig.TILGANG_CACHE;
import static no.nav.saf.domain.DomainConstants.PEP6D;

import io.lettuce.core.RedisException;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.cache.KeyGeneratorDistributedCaching;
import no.nav.saf.cache.KeyGeneratorLocalCaching;
import no.nav.saf.cache.RedisCacheConfig;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.PoolException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Dekker følgende policies i saf:
 * <p>
 * Lokal caching er kun relevant for dokumentoversiktene og brukes i journalpostMapperDto.java
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component(PEP6D)
public class Pep6dImpl implements Pep<TilgangDokumentvariant> {

	private final Cache tilgangCache;
	private final AbacService abacService;

	@Inject
	public Pep6dImpl(@Named(RedisCacheConfig.MANAGER_DISTRIBUTED) CacheManager redisCacheManager, AbacService abacService) {
		this.tilgangCache = redisCacheManager.getCache(TILGANG_CACHE);
		this.abacService = abacService;
	}

	@Override
	public XacmlResponse verifyAccessXacmlResponse(TilgangDokumentvariant ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.warn("Pep6d mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll");
			return XacmlResponse.deny();
		}

		if (isSkjermingPresent(ressurs)) {
			if (isVariantformatNull(ressurs)) {
				log.warn("Pep6d mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll. Variantformat=null. journalpostId={} og dokumentinfoId={}",
						ressurs.getJournalpostId(), ressurs.getDokumentInfoId());
				return XacmlResponse.deny();
			}

			Pep.traceLogPepStarted(PEP6D, ressurs);

			String tilgangKeyDistributedCaching = KeyGeneratorDistributedCaching.getKeyForPep6d(
					safRequestContext.getSecurityContext().getSubjectId(),
					ressurs.getJournalpostId(),
					ressurs.getDokumentInfoId(),
					ressurs.getVariantformat().name(),
					ressurs.getSkjerming().name());

			String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep6d(
					ressurs.getJournalpostId(),
					ressurs.getDokumentInfoId(),
					ressurs.getVariantformat().name(),
					ressurs.getSkjerming().name());

			try {
				XacmlResponse response = fetchXacmlResponse(ressurs, safRequestContext, tilgangKeyDistributedCaching);
				if(response == null) {
					return XacmlResponse.deny();
				}
				safRequestContext.getRequestCache().putObject(tilgangKeyLocalCaching, decide(response.getDecision()));
				return response;
			} catch (RedisSystemException | RedisException | PoolException | Cache.ValueRetrievalException | RedisConnectionFailureException e) {
				// Ting skal fremdeles snurre selv om man ikke får kontakt med redis
				XacmlResponse response = hasDokumentFilAccess(ressurs, safRequestContext);
				safRequestContext.getRequestCache().putObject(tilgangKeyLocalCaching, decide(response.getDecision()));
				return response;
			} finally {
				Pep.traceLogPepFinished(PEP6D, ressurs);
			}
		} else {
			String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep6d(
					ressurs.getJournalpostId(),
					ressurs.getDokumentInfoId(),
					isVariantformatNull(ressurs) ? null : ressurs.getVariantformat().name(),
					null);
			safRequestContext.getRequestCache().putObject(tilgangKeyLocalCaching, true);
			return XacmlResponse.permit();
		}
	}

	private XacmlResponse fetchXacmlResponse(TilgangDokumentvariant ressurs, SafRequestContext safRequestContext, String tilgangKeyDistributedCaching) {
		XacmlResponse cachedResponse = tilgangCache.get(tilgangKeyDistributedCaching, XacmlResponse.class);
		if(cachedResponse == null) {
			XacmlResponse abacResponse = hasDokumentFilAccess(ressurs, safRequestContext);
			if(abacResponse == null) {
				return XacmlResponse.deny();
			}
			if(decide(abacResponse.getDecision())) {
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

	private XacmlResponse hasDokumentFilAccess(TilgangDokumentvariant ressurs, SafRequestContext safRequestContext) {
		XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext());
		request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_DOKUMENT_FIL);
		request.resource(RESOURCE_SAF_SKJERMING, ressurs.getSkjerming().name());
		return abacService.evaluate(request);
	}

	private boolean isSkjermingPresent(TilgangDokumentvariant ressurs) {
		return ressurs.getSkjerming() != null;
	}

	private boolean isVariantformatNull(TilgangDokumentvariant ressurs) {
		return ressurs.getVariantformat() == null;
	}
}
