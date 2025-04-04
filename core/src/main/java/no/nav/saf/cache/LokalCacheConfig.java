package no.nav.saf.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class LokalCacheConfig {
	public static final String BIDRAG_SAK_BY_SAKID_CACHE = "bidragSakerBySakId";
	public static final String FPSAK_RELEVANTE_PARTER_BY_SAKID_CACHE = "hentRelevanteParter";
	public static final String TILGANGSMODELL_REPO_BRUKER_CACHE = "tilgangsmodellRepoBruker";
	public static final String AZURE_CLIENT_CREDENTIAL_TOKEN_CACHE = "azureClientCredentialToken";

	@Bean
	@Primary
	@Profile({"nais", "local" })
	CacheManager cacheManager() {
		SimpleCacheManager manager = new SimpleCacheManager();
		manager.setCaches(Arrays.asList(
				// Lokale caches
				new CaffeineCache(TILGANGSMODELL_REPO_BRUKER_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(500)
						.recordStats()
						.build()),
				new CaffeineCache(BIDRAG_SAK_BY_SAKID_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(500)
						.recordStats()
						.build()),
				new CaffeineCache(FPSAK_RELEVANTE_PARTER_BY_SAKID_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(500)
						.recordStats()
						.build()),
				new CaffeineCache(AZURE_CLIENT_CREDENTIAL_TOKEN_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(50, TimeUnit.MINUTES)
						.maximumSize(10)
						.recordStats()
						.build())
		));
		return manager;
	}
}
