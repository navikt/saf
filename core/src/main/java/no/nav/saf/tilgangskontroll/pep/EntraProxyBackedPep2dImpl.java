package no.nav.saf.tilgangskontroll.pep;

import io.lettuce.core.RedisException;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.nav.entraproxy.EntraProxyConsumer;
import no.nav.saf.anticorruptionlayer.nav.entraproxy.EntraProxyTematilgangResponse;
import no.nav.saf.cache.KeyGeneratorDistributedCaching;
import no.nav.saf.cache.KeyGeneratorLocalCaching;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.reasons.TemaReason;
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
public class EntraProxyBackedPep2dImpl extends StandardEntraProxyBackedPep<TilgangSak> {

	private static final String MANGLER_RESSURS_MELDING = "Pep2d (tema-tilgang) mangler data om sak. Tilgang gis likevel for at {} skal kunne knytte dokument til sak og bruker.";

	private final Cache tilgangCache;
	private final EntraProxyConsumer entraProxyConsumer;

	public EntraProxyBackedPep2dImpl(@Qualifier(VALKEY_CACHE_MANAGER) CacheManager redisCacheManager,
									 EntraProxyConsumer entraProxyConsumer) {
		this.tilgangCache = redisCacheManager.getCache(VALKEY_DOKUMENT_TILGANG_CACHE);
		this.entraProxyConsumer = entraProxyConsumer;
	}

	@Override
	PepAnswer verifyNavIdentAccessToTema(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.info(MANGLER_RESSURS_MELDING, "saksbehandler");
			return permit();
		}

		traceLogPepStarted(PEP2D, ressurs);
		Tema tema = ressurs.getTema();
		String tilgangKeyDistributedCaching = KeyGeneratorDistributedCaching.getKeyForPep2d(safRequestContext.getUserId(), tema);
		String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep2d(tema);

		PepAnswer pepAnswer;
		// Try-catch er fordi redis ikke fungerer lokalt
		try {
			pepAnswer = getPepAnswerFromEntraProxy(ressurs, safRequestContext, tilgangKeyDistributedCaching);

			safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, pepAnswer);
			return pepAnswer;
		} catch (RedisSystemException | RedisException | PoolException | Cache.ValueRetrievalException |
				 RedisConnectionFailureException _) {
			// Ting skal fremdeles snurre selv om man ikke får kontakt med redis
			pepAnswer = getPepAnswerFromEntraProxy(ressurs, safRequestContext, null);

			safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, pepAnswer);
			return pepAnswer;
		} finally {
			traceLogPepFinished(PEP2D, ressurs);
		}
	}

	@Override
	public PepAnswer verifyAccessForSystemUser(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.info(MANGLER_RESSURS_MELDING, "system");
			return permit();
		}

		traceLogPepStarted(PEP2D, ressurs);
		Tema tema = ressurs.getTema();
		String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep2d(tema);

		boolean decision = safRequestContext.isSystemAndVariantformatOriginal() || safRequestContext.getSecurityContext().hasDokumentTilgangEntraRole(tema);

		PepAnswer pepAnswer = decision ? permit() : PepAnswer.deny(new TemaReason(
				"cause_0013_ikketilgangtilDokumenttema", "saf_pep2d", "mangler_tema", tema));

		safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, pepAnswer);
		traceLogPepFinished(PEP2D, ressurs);

		return pepAnswer;
	}

	private PepAnswer getPepAnswerFromEntraProxy(TilgangSak ressurs, SafRequestContext safRequestContext, String tilgangKeyDistributedCaching) {
		PepAnswer cachedResponse = getFromCache(tilgangKeyDistributedCaching);
		if (cachedResponse != null) {
			return cachedResponse;
		}

		EntraProxyTematilgangResponse response = entraProxyConsumer.hentTematilgangForNavAnsatt(safRequestContext);
		Tema tema = ressurs.getTema();

		if (response.harTilgangTilTema(tema)) {
			updateCache(tilgangKeyDistributedCaching, permit());
			return permit();
		}

		TemaReason temaReason = new TemaReason("cause_0013_ikketilgangtilDokumenttema", "saf_pep2d", "tematilgang_nok", tema);
		return PepAnswer.deny(temaReason);
	}

	private PepAnswer getFromCache(String key) {
		return key != null ? tilgangCache.get(key, PepAnswer.class) : null;
	}

	private void updateCache(String key, PepAnswer pepAnswer) {
		if (key != null) {
			tilgangCache.put(key, pepAnswer);
		}
	}
}
