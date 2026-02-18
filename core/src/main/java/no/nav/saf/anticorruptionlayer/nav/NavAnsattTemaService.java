package no.nav.saf.anticorruptionlayer.nav;

import io.lettuce.core.RedisException;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.nav.entraproxy.EntraProxyConsumer;
import no.nav.saf.anticorruptionlayer.nav.entraproxy.EntraProxyTematilgangResponse;
import no.nav.saf.cache.KeyGeneratorDistributedCaching;
import no.nav.saf.cache.KeyGeneratorLocalCaching;
import no.nav.saf.cache.ValkeyCacheConfig;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.PepAnswer;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.PoolException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

import static no.nav.saf.cache.ValkeyCacheConfig.VALKEY_CACHE_ENTRY_TTL;

@Slf4j
@Component
public class NavAnsattTemaService {

	private final EntraProxyConsumer entraProxyConsumer;
	private final StringRedisTemplate stringRedisTemplate;

	public NavAnsattTemaService(EntraProxyConsumer entraProxyConsumer,
								StringRedisTemplate stringRedisTemplate) {
		this.entraProxyConsumer = entraProxyConsumer;
		this.stringRedisTemplate = stringRedisTemplate;
	}

	/// Henter tema tilgang entries fra valkey cache eller entra-proxy
	///
	/// Populererer lokal request cache med tematilgango
	public void registerRequestCacheTemaTilgang(SafRequestContext safRequestContext) {
		if (safRequestContext.isUserIdNavAnsatt()) {
			final String navIdent = safRequestContext.getUserId();
			getValkeyCacheTemaTilganger(navIdent).forEach(tema -> {
				try {
					String keyForPep2d = KeyGeneratorLocalCaching.getKeyForPep2d(Tema.valueOf(tema));
					safRequestContext.getRequestCache().putDecision(keyForPep2d, PepAnswer.permit());
				} catch(IllegalArgumentException e) {
					// noop
				}
			});
		}
	}

	///  Evaluerer om Nav ansatt har tilgang til tema
	///
	/// Ser etter tilgang i prioritert rekkefølge:
	/// 1. Request cache
	/// 2. Valkey
	/// 3. Entra-proxy
	///
	/// @return `true` hvis Nav ansatt har tilgang til tema, `false` ellers
	public boolean harTemaTilgang(SafRequestContext safRequestContext, Tema tema) {
		if (safRequestContext.isUserIdNavAnsatt()) {
			final String navIdent = safRequestContext.getUserId();
			PepAnswer requestCacheDecision = getRequestCacheDecision(safRequestContext, tema);
			if (requestCacheDecision == null) {
				try {
					return getValkeyCacheTemaTilgang(navIdent, tema);
				} catch (RedisSystemException | RedisException | PoolException |
						 RedisConnectionFailureException e) {
					return getTemaGroupsForNavIdent(navIdent).contains(tema.name());
				}
			} else {
				return requestCacheDecision.isPermit();
			}
		}
		return false;
	}

	private Set<String> getValkeyCacheTemaTilganger(String navIdent) {
		String valkeyNavIdentKey = KeyGeneratorDistributedCaching.getNavIdentTemaKey(navIdent);
		if (stringRedisTemplate.hasKey(valkeyNavIdentKey)) {
			stringRedisTemplate.expire(valkeyNavIdentKey, VALKEY_CACHE_ENTRY_TTL);
			return stringRedisTemplate.opsForSet().members(valkeyNavIdentKey);
		} else {
			return valkeyCacheMiss(navIdent, valkeyNavIdentKey);
		}
	}

	private boolean getValkeyCacheTemaTilgang(String navIdent, Tema tema) {
		String valkeyNavIdentKey = KeyGeneratorDistributedCaching.getNavIdentTemaKey(navIdent);
		if (stringRedisTemplate.hasKey(valkeyNavIdentKey)) {
			Boolean temaMedlem = stringRedisTemplate.opsForSet().isMember(valkeyNavIdentKey, tema.name());
			return temaMedlem != null && temaMedlem;
		} else {
			return valkeyCacheMiss(navIdent, valkeyNavIdentKey).contains(tema.name());
		}
	}

	private Set<String> valkeyCacheMiss(String navIdent, String valkeyNavIdentKey) {
		Set<String> groups = getTemaGroupsForNavIdent(navIdent);
		if (groups.isEmpty()) {
			stringRedisTemplate.opsForSet().add(valkeyNavIdentKey, ValkeyCacheConfig.NO_GROUPS);
			stringRedisTemplate.expire(valkeyNavIdentKey, VALKEY_CACHE_ENTRY_TTL);
			return Set.of();
		} else {
			stringRedisTemplate.opsForSet().add(valkeyNavIdentKey, groups.toArray(String[]::new));
			stringRedisTemplate.expire(valkeyNavIdentKey, VALKEY_CACHE_ENTRY_TTL);
			return groups;
		}
	}

	private static PepAnswer getRequestCacheDecision(SafRequestContext safRequestContext, Tema tema) {
		String localCacheKey = KeyGeneratorLocalCaching.getKeyForPep2d(tema);
		return safRequestContext.getRequestCache().getCachedDecision(localCacheKey);
	}

	private Set<String> getTemaGroupsForNavIdent(String navIdent) {
		EntraProxyTematilgangResponse entraProxyTematilgangResponse = entraProxyConsumer.hentTematilgangForNavAnsatt(navIdent);
		return entraProxyTematilgangResponse.temaer();
	}
}
