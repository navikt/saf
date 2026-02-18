package no.nav.saf.endpoints.testconfig;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static no.nav.saf.cache.LokalCacheConfig.BIDRAG_SAK_BY_SAKID_CACHE;
import static no.nav.saf.cache.LokalCacheConfig.ENTRA_PROXY_TEMA_CACHE;
import static no.nav.saf.cache.LokalCacheConfig.FPSAK_RELEVANTE_PARTER_BY_SAKID_CACHE;
import static no.nav.saf.cache.LokalCacheConfig.TILGANGSMODELL_REPO_BRUKER_CACHE;

@Configuration
@Profile("itest")
public class LokalCacheTestConfig {
	@Bean
	@Primary
	CacheManager cacheManager() {
		SimpleCacheManager manager = new SimpleCacheManager();
		manager.setCaches(Arrays.asList(
				new CaffeineCache(ENTRA_PROXY_TEMA_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(0, TimeUnit.MINUTES)
						.maximumSize(0)
						.build()),
				new CaffeineCache(TILGANGSMODELL_REPO_BRUKER_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(0, TimeUnit.MINUTES)
						.maximumSize(0)
						.build()),
				new CaffeineCache(BIDRAG_SAK_BY_SAKID_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(0, TimeUnit.MINUTES)
						.maximumSize(0)
						.build()),
				new CaffeineCache(FPSAK_RELEVANTE_PARTER_BY_SAKID_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(0, TimeUnit.MINUTES)
						.maximumSize(0)
						.build())
		));
		return manager;
	}

}
