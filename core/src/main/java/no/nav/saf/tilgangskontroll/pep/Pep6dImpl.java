package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_DOKUMENT_FIL;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_SKJERMING;
import static no.nav.saf.cache.KeyGeneratorDistributedCaching.getKeyForPep6dDistributedCaching;
import static no.nav.saf.cache.KeyGeneratorLocalCaching.getKeyForPep6dLocalCaching;
import static no.nav.saf.cache.RedisCacheConfig.TILGANG_CACHE;
import static no.nav.saf.domain.DomainConstants.PEP6D;

import io.lettuce.core.RedisException;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.cache.RedisCacheConfig;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
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
	public boolean hasAccess(TilgangDokumentvariant ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.warn("Pep6d mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll");
			return false;
		}

		if (isSkjermingPresent(ressurs)) {
			Pep.traceLogPepStarted(PEP6D, ressurs);

			String tilgangKeyDistributedCaching = getKeyForPep6dDistributedCaching(
					safRequestContext.getSecurityContext().getSaksbehandlerId(),
					ressurs.getJournalpostId(),
					ressurs.getDokumentInfoId(),
					ressurs.getVariantformat() == null ? null : ressurs.getVariantformat().name(),
					ressurs.getVariantformat() == null ? null : ressurs.getVariantformat().name());

			String tilgangKeyLocalCaching = getKeyForPep6dLocalCaching(
					ressurs.getJournalpostId(),
					ressurs.getDokumentInfoId(),
					ressurs.getVariantformat() == null ? null : ressurs.getVariantformat().name(),
					ressurs.getVariantformat() == null ? null : ressurs.getVariantformat().name());

			try {
				boolean decide = decide(tilgangCache.get(tilgangKeyDistributedCaching,
						() -> hasDokumentFilAccess(ressurs, safRequestContext)));
				safRequestContext.getRequestCache().putObject(tilgangKeyLocalCaching, decide);
				return decide;
			} catch (RedisException | PoolException | Cache.ValueRetrievalException e) {
				boolean decide = decide(hasDokumentFilAccess(ressurs, safRequestContext));
				safRequestContext.getRequestCache().putObject(tilgangKeyLocalCaching, decide);
				return decide;
			} finally {
				Pep.traceLogPepFinished(PEP6D, ressurs);
			}
		} else {
			return true;
		}
	}

	private boolean decide(Decision decision) {
		return Decision.PERMIT.equals(decision);
	}

	private Decision hasDokumentFilAccess(TilgangDokumentvariant ressurs, SafRequestContext safRequestContext) {
		XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext().getOidcTokenBody());
		request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_DOKUMENT_FIL);
		request.resource(RESOURCE_SAF_SKJERMING, ressurs.getSkjerming().name());

		Pep.traceLogPepStarted(PEP6D, ressurs);
		XacmlResponse response = abacService.evaluate(request);
		Pep.traceLogPepFinished(PEP6D, ressurs);

		return response.getDecision();
	}

	private boolean isSkjermingPresent(TilgangDokumentvariant ressurs) {
		return ressurs.getSkjerming() != null;
	}
}
