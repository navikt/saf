package no.nav.saf.anticorruptionlayer.nav;

import com.microsoft.graph.models.User;
import io.lettuce.core.RedisException;
import no.nav.saf.cache.ValkeyCacheConfig;
import org.springframework.cache.Cache;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.connection.PoolException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static no.nav.saf.cache.KeyGeneratorDistributedCaching.getKeyForSaksbehandlerGroupMembership;
import static no.nav.saf.cache.ValkeyCacheConfig.VALKEY_CACHE_ENTRY_TTL;

@Component
public class MsGraphEntraGroupMembershipService {
	private static final String[] NO_GROUPS = new String[]{"NO_GROUPS"};
	private final MsGraphConsumer msGraphConsumer;
	private final StringRedisTemplate stringRedisTemplate;
	private final ValkeyCacheConfig.ValkeyGrupperMedlemskapCacheConfiguration valkeyGrupperMedlemskapCacheConfiguration;

	public MsGraphEntraGroupMembershipService(MsGraphConsumer msGraphConsumer, StringRedisTemplate stringRedisTemplate,
											  ValkeyCacheConfig.ValkeyGrupperMedlemskapCacheConfiguration valkeyGrupperMedlemskapCacheConfiguration) {
		this.msGraphConsumer = msGraphConsumer;
		this.stringRedisTemplate = stringRedisTemplate;
		this.valkeyGrupperMedlemskapCacheConfiguration = valkeyGrupperMedlemskapCacheConfiguration;
	}

	public synchronized boolean isUserInGroup(String navIdent, UUID azureAdGroup) {
		String valkeyKeyForIdent = valkeyGrupperMedlemskapCacheConfiguration.valkeyKeyPrefix() + getKeyForSaksbehandlerGroupMembership(navIdent);

		try {
			if (!stringRedisTemplate.hasKey(valkeyKeyForIdent)) {
				Set<String> groups = getGroupsForIdent(navIdent);

				if (groups.isEmpty()) {
					stringRedisTemplate.opsForSet().add(valkeyKeyForIdent, NO_GROUPS);
					stringRedisTemplate.expire(valkeyKeyForIdent, VALKEY_CACHE_ENTRY_TTL);
					return false;
				} else {
					stringRedisTemplate.opsForSet().add(valkeyKeyForIdent, groups.toArray(String[]::new));
					stringRedisTemplate.expire(valkeyKeyForIdent, VALKEY_CACHE_ENTRY_TTL);
					return groups.contains(azureAdGroup.toString());
				}
			} else {
				Boolean isMemberOfGroup = stringRedisTemplate.opsForSet().isMember(valkeyKeyForIdent, azureAdGroup.toString());
				stringRedisTemplate.expire(valkeyKeyForIdent, VALKEY_CACHE_ENTRY_TTL); // oppdater TTL on read, samme oppfoersel som spring sin Cache.read(..)
				return isMemberOfGroup != null && isMemberOfGroup;
			}
		} catch (RedisSystemException | RedisException | PoolException | Cache.ValueRetrievalException |
				 RedisConnectionFailureException e) {
			// Ting skal fremdeles snurre selv om man ikke får kontakt med redis
			return getGroupsForIdent(navIdent).contains(azureAdGroup.toString());
		}
	}

	private Set<String> getGroupsForIdent(String navIdent) {
		Optional<User> user = msGraphConsumer.getUser(navIdent);
		return msGraphConsumer.getRelevantGroupsForUser(user);
	}
}
