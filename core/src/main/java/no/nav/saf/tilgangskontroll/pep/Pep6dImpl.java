package no.nav.saf.tilgangskontroll.pep;

import static no.nav.saf.cache.RedisCacheConfig.TILGANG_CACHE;

import io.lettuce.core.RedisException;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.cache.RedisCacheConfig;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.PoolException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Dekker følgende policies i saf:
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component("pep6d")
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

		if (ressurs.getSkjerming() != null) {
			Pep.traceLogPepStarted("pep6", ressurs);

			String tilgangKey = "tilgang:" + safRequestContext.getSecurityContext()
					.getSaksbehandlerId() + ":ressurstype:dokument_fil" + ":variantformat:" + ressurs.getVariantformat()
					+ ":skjerming=" + ressurs.getSkjerming();
			try {
				boolean decide = decide(hasDokumentFilAccess(ressurs, safRequestContext));
				safRequestContext.getRequestCache().putObject(tilgangKey, decide);
				return decide;
			} catch (RedisException | PoolException | Cache.ValueRetrievalException e) {
				boolean decide = decide(hasDokumentFilAccess(ressurs, safRequestContext));
				safRequestContext.getRequestCache().putObject(tilgangKey, decide);
				return decide;
			} finally {
				Pep.traceLogPepFinished("pep6", ressurs);
			}
		} else {
			return true;
		}
	}

	private boolean decide(Decision decision) {
		return Decision.PERMIT.equals(decision);
	}

	private Decision hasDokumentFilAccess(TilgangDokumentvariant ressurs, SafRequestContext safRequestContext) {
		XacmlRequest request = new XacmlRequest();
		//TODO Populate request and perform call to pdp
		return Decision.PERMIT;
	}
}
