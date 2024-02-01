package no.nav.saf.tilgangskontroll.pep;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.PoolException;
import org.springframework.stereotype.Component;

import static no.nav.saf.cache.RedisCacheConfig.TILGANG_CACHE;
import static no.nav.saf.domain.DomainConstants.PEP6D;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_DOKUMENT_FIL;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_SKJERMING;
import static no.nav.saf.tilgangskontroll.pep.AbacAnswer.deny;
import static no.nav.saf.tilgangskontroll.pep.AbacAnswer.permit;

/**
 * Dekker følgende policies i saf:
 * <p>
 * Lokal caching er kun relevant for dokumentoversiktene og brukes i journalpostDtoMapper.java
 */
@Slf4j
@Component(PEP6D)
public class Pep6dImpl extends Pep<TilgangDokumentvariant> {

	private final Cache tilgangCache;
	private final AbacService abacService;

	@Autowired
	public Pep6dImpl(@Qualifier(RedisCacheConfig.MANAGER_DISTRIBUTED) CacheManager redisCacheManager, AbacService abacService) {
		this.tilgangCache = redisCacheManager.getCache(TILGANG_CACHE);
		this.abacService = abacService;
	}

	@Override
	public XacmlResponse verifyAbacPdpDecision(TilgangDokumentvariant ressurs, SafRequestContext safRequestContext) {
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

			traceLogPepStarted(PEP6D, ressurs);

			String tilgangKeyDistributedCaching = KeyGeneratorDistributedCaching.getKeyForPep6d(
					safRequestContext.getUserId(),
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
				if (response == null) {
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
				traceLogPepFinished(PEP6D, ressurs);
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

	@Override
	public AbacAnswer verifyAzureClientCredentialFlowAccess(TilgangDokumentvariant ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.warn("Pep6d mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll. Azure ccf.");
			return deny(AbacAnswer.AbacDenyReason.builder()
					.cause("dokumentvariant_mangler_data").policy("saf_pep6d").rule("dokumentvariant_er_null")
					.build());
		}

		if (isSkjermingPresent(ressurs)) {
			if (isVariantformatNull(ressurs)) {
				log.warn("Pep6d mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll. Variantformat=null. journalpostId={} og dokumentinfoId={}. Azure ccf.",
						ressurs.getJournalpostId(), ressurs.getDokumentInfoId());
				return deny(AbacAnswer.AbacDenyReason.builder()
						.cause("dokumentvariant_mangler_variantformat").policy("saf_pep6d").rule("dokumentvariant_skjermet_og_variantformat_er_null")
						.build());
			}

			traceLogPepStarted(PEP6D, ressurs);
			String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep6d(
					ressurs.getJournalpostId(),
					ressurs.getDokumentInfoId(),
					ressurs.getVariantformat().name(),
					ressurs.getSkjerming().name());

			boolean decision = !isSkjermingPresent(ressurs);
			safRequestContext.getRequestCache().putObject(tilgangKeyLocalCaching, decision);
			traceLogPepFinished(PEP6D, ressurs);
			return decision ? permit() : deny(AbacAnswer.AbacDenyReason.builder()
					.cause("dokumentvariant_skjermet").policy("saf_pep6d").rule("dokumentvariant_skjermet")
					.build());
		} else {
			String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep6d(
					ressurs.getJournalpostId(),
					ressurs.getDokumentInfoId(),
					isVariantformatNull(ressurs) ? null : ressurs.getVariantformat().name(),
					null);
			safRequestContext.getRequestCache().putObject(tilgangKeyLocalCaching, true);
			return permit();
		}
	}

	private XacmlResponse fetchXacmlResponse(TilgangDokumentvariant ressurs, SafRequestContext safRequestContext, String tilgangKeyDistributedCaching) {
		XacmlResponse cachedResponse = tilgangCache.get(tilgangKeyDistributedCaching, XacmlResponse.class);
		if (cachedResponse == null) {
			XacmlResponse abacResponse = hasDokumentFilAccess(ressurs, safRequestContext);
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
