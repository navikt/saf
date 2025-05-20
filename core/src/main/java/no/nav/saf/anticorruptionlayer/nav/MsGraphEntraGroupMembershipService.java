package no.nav.saf.anticorruptionlayer.nav;

import com.microsoft.graph.models.User;
import no.nav.saf.cache.ValkeyCacheConfig;
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

		if (!stringRedisTemplate.hasKey(valkeyKeyForIdent)) {
			Optional<User> user = msGraphConsumer.getUser(navIdent);
			Set<String> groups = msGraphConsumer.getRelevantGroupsForUser(user);

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
	}
}
