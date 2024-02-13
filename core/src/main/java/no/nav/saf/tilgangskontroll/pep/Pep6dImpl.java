package no.nav.saf.tilgangskontroll.pep;

import io.lettuce.core.RedisException;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.cache.KeyGeneratorDistributedCaching;
import no.nav.saf.cache.KeyGeneratorLocalCaching;
import no.nav.saf.cache.RedisCacheConfig;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import no.nav.saf.tilgangskontroll.pep.reasons.AbacDenyReason;
import no.nav.saf.tilgangskontroll.pep.reasons.SkjermingReason;
import no.nav.saf.tilgangskontroll.pep.reasons.UkjentReason;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.PoolException;
import org.springframework.stereotype.Component;

import java.util.Collections;

import static no.nav.saf.cache.RedisCacheConfig.TILGANG_CACHE;
import static no.nav.saf.domain.DomainConstants.PEP6D;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_DOKUMENT_FIL;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_SKJERMING;
import static no.nav.saf.tilgangskontroll.abac.dto.response.AdviceStringUtil.getAdvicesMap;
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
	public AbacAnswer verifyAbacPdpDecision(TilgangDokumentvariant ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.warn("Pep6d mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll");
			return AbacAnswer.deny(new UkjentReason());
		}

		if (isSkjermingPresent(ressurs)) {
			if (isVariantformatNull(ressurs)) {
				log.warn("Pep6d mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll. Variantformat=null. journalpostId={} og dokumentinfoId={}",
						ressurs.getJournalpostId(), ressurs.getDokumentInfoId());
				return AbacAnswer.deny(new UkjentReason());
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
				AbacAnswer response = fetchXacmlResponse(ressurs, safRequestContext, tilgangKeyDistributedCaching);
				if (response == null) {
					return AbacAnswer.deny(new UkjentReason());
				}
				safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, response);
				return response;
			} catch (RedisSystemException | RedisException | PoolException | Cache.ValueRetrievalException | RedisConnectionFailureException e) {
				// Ting skal fremdeles snurre selv om man ikke får kontakt med redis
				AbacAnswer response = mapXacmlResponse(hasDokumentFilAccess(ressurs, safRequestContext));
				safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, response);
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
			safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, AbacAnswer.permit());
			return AbacAnswer.permit();
		}
	}

	@Override
	public AbacAnswer verifyAzureClientCredentialFlowAccess(TilgangDokumentvariant ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.warn("Pep6d mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll. Azure ccf.");
			return AbacAnswer.deny(new SkjermingReason(
					"dokumentvariant_mangler_data", "saf_pep6d", "dokumentvariant_er_null"
					));
		}

		if (isSkjermingPresent(ressurs)) {
			if (isVariantformatNull(ressurs)) {
				log.warn("Pep6d mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll. Variantformat=null. journalpostId={} og dokumentinfoId={}. Azure ccf.",
						ressurs.getJournalpostId(), ressurs.getDokumentInfoId());
				return AbacAnswer.deny(
						new SkjermingReason("dokumentvariant_mangler_variantformat", "saf_pep6d", "dokumentvariant_skjermet_og_variantformat_er_null"));
			}

			traceLogPepStarted(PEP6D, ressurs);
			String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep6d(
					ressurs.getJournalpostId(),
					ressurs.getDokumentInfoId(),
					ressurs.getVariantformat().name(),
					ressurs.getSkjerming().name());

			boolean decision = !isSkjermingPresent(ressurs);
			traceLogPepFinished(PEP6D, ressurs);
			AbacAnswer abacAnswer = decision ? permit() : AbacAnswer.deny(new SkjermingReason(
					"dokumentvariant_skjermet", "saf_pep6d", "dokumentvariant_skjermet"
					));
			safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, abacAnswer);
			return abacAnswer;
		} else {
			String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep6d(
					ressurs.getJournalpostId(),
					ressurs.getDokumentInfoId(),
					isVariantformatNull(ressurs) ? null : ressurs.getVariantformat().name(),
					null);
			safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, AbacAnswer.permit());
			return permit();
		}
	}

	@Override
	AbacAnswer translateToDenyReasonCode(XacmlResponse xacmlResponse) {
		return AbacAnswer.deny(new SkjermingReason(getAdvicesMap(xacmlResponse.getAdvices())));
	}

	private AbacAnswer fetchXacmlResponse(TilgangDokumentvariant ressurs, SafRequestContext safRequestContext, String tilgangKeyDistributedCaching) {
		XacmlResponse cachedResponse = tilgangCache.get(tilgangKeyDistributedCaching, XacmlResponse.class);
		if (cachedResponse == null) {
			XacmlResponse abacResponse = hasDokumentFilAccess(ressurs, safRequestContext);
			if (abacResponse == null) {
				return AbacAnswer.deny(new SkjermingReason(Collections.emptyMap()));  // ev. UkjentReason?
			}
			if (abacResponse.isPermit()) {
				tilgangCache.put(tilgangKeyDistributedCaching, abacResponse);
			}
			return mapXacmlResponse(abacResponse); // siden put kun gjøres om decide = true er vel denna alltid = permit
		} else {
			return mapXacmlResponse(cachedResponse);
		}
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
