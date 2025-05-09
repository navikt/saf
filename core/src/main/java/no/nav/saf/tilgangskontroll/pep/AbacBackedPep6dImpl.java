package no.nav.saf.tilgangskontroll.pep;

import io.lettuce.core.RedisException;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.cache.KeyGeneratorDistributedCaching;
import no.nav.saf.cache.KeyGeneratorLocalCaching;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import no.nav.saf.tilgangskontroll.pep.reasons.SkjermingReason;
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
import static no.nav.saf.domain.DomainConstants.PEP6D;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_DOKUMENT_FIL;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_SKJERMING;
import static no.nav.saf.tilgangskontroll.pep.PepAnswer.permit;

/**
 * Dekker følgende policies i saf:
 * <p>
 * Lokal caching er kun relevant for dokumentoversiktene og brukes i journalpostDtoMapper.java
 */
@Slf4j
@Component(PEP6D)
public class AbacBackedPep6dImpl extends StandardAbacBackedPep<TilgangDokumentvariant> {

	private final Cache tilgangCache;
	private final AbacService abacService;

	@Autowired
	public AbacBackedPep6dImpl(@Qualifier(VALKEY_CACHE_MANAGER) CacheManager redisCacheManager, AbacService abacService) {
		this.tilgangCache = redisCacheManager.getCache(VALKEY_DOKUMENT_TILGANG_CACHE);
		this.abacService = abacService;
	}

	@Override
	public PepAnswer verifyAbacPdpDecision(TilgangDokumentvariant ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.warn("Pep6d mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll");
			return PepAnswer.deny(new UkjentEllerTekniskReason());
		}

		if (isSkjermingPresent(ressurs)) {
			if (isVariantformatNull(ressurs)) {
				log.warn("Pep6d mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll. Variantformat=null. journalpostId={} og dokumentinfoId={}",
						ressurs.getJournalpostId(), ressurs.getDokumentInfoId());
				return PepAnswer.deny(new UkjentEllerTekniskReason());
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
				PepAnswer response = fetchXacmlResponse(ressurs, safRequestContext, tilgangKeyDistributedCaching);
				if (response == null) {
					log.warn("Pep6d mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll. Tomt svar fra ABAC. journalpostId={} og dokumentinfoId={}",
							ressurs.getJournalpostId(), ressurs.getDokumentInfoId());
					return PepAnswer.deny(new UkjentEllerTekniskReason());
				}
				safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, response);
				return response;
			} catch (RedisSystemException | RedisException | PoolException | Cache.ValueRetrievalException | RedisConnectionFailureException e) {
				// Ting skal fremdeles snurre selv om man ikke får kontakt med redis
				PepAnswer response = mapToAbacAnswer(hasDokumentFilAccess(ressurs, safRequestContext));
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
			safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, PepAnswer.permit());
			return PepAnswer.permit();
		}
	}

	@Override
	public PepAnswer verifyAzureClientCredentialFlowAccess(TilgangDokumentvariant ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.warn("Pep6d mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll. Azure ccf.");
			return PepAnswer.deny(new SkjermingReason(
					"dokumentvariant_mangler_data", "saf_pep6d", "dokumentvariant_er_null"
					));
		}

		if (isSkjermingPresent(ressurs)) {
			if (isVariantformatNull(ressurs)) {
				log.warn("Pep6d mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll. Variantformat=null. journalpostId={} og dokumentinfoId={}. Azure ccf.",
						ressurs.getJournalpostId(), ressurs.getDokumentInfoId());
				return PepAnswer.deny(
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
			PepAnswer pepAnswer = decision ? permit() : PepAnswer.deny(new SkjermingReason(
					"dokumentvariant_skjermet", "saf_pep6d", "dokumentvariant_skjermet"
					));
			safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, pepAnswer);
			return pepAnswer;
		} else {
			String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep6d(
					ressurs.getJournalpostId(),
					ressurs.getDokumentInfoId(),
					isVariantformatNull(ressurs) ? null : ressurs.getVariantformat().name(),
					null);
			safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, PepAnswer.permit());
			return permit();
		}
	}

	@Override
	protected PepAnswer translateToDenyReasonCode(XacmlResponse xacmlResponse) {
		return PepAnswer.deny(new SkjermingReason(xacmlResponse.getAdvicesMap()));
	}

	private PepAnswer fetchXacmlResponse(TilgangDokumentvariant ressurs, SafRequestContext safRequestContext, String tilgangKeyDistributedCaching) {
		XacmlResponse cachedResponse = tilgangCache.get(tilgangKeyDistributedCaching, XacmlResponse.class);
		if (cachedResponse == null) {
			XacmlResponse abacResponse = hasDokumentFilAccess(ressurs, safRequestContext);
			if (abacResponse == null) {
				return PepAnswer.deny(
						new SkjermingReason("cause-0001-manglerrolle", "saf_skjerming", "rolle_NOK"));
			}
			if (abacResponse.isPermit()) {
				tilgangCache.put(tilgangKeyDistributedCaching, abacResponse);
			}
			return mapToAbacAnswer(abacResponse);
		} else {
			return mapToAbacAnswer(cachedResponse);
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
