package no.nav.saf.endpoints.testconfig;

import static no.nav.saf.cache.LokalCacheConfig.GRAPHQL_QUERY_CACHE;
import static no.nav.saf.cache.LokalCacheConfig.HENT_JOURNALPOSTBULK_CACHE;
import static no.nav.saf.cache.LokalCacheConfig.PENSJON_SAK_SAMMENDRAG_LISTE_CACHE;
import static no.nav.saf.cache.LokalCacheConfig.SAKER_BY_AKTOER_ID_CACHE;
import static no.nav.saf.cache.LokalCacheConfig.SAKER_BY_FAGSAK_ID_CACHE;
import static no.nav.saf.cache.LokalCacheConfig.SAKER_BY_ORG_NR_CACHE;
import static no.nav.saf.cache.LokalCacheConfig.SAK_BY_SAKID_CACHE;
import static no.nav.saf.cache.LokalCacheConfig.TILGANGSMODELL_REPO_BRUKER_CACHE;

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

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Configuration
@Profile("itest")
public class LokalCacheTestConfig {

	@Bean
	@Primary
	CacheManager cacheManager() {
		SimpleCacheManager manager = new SimpleCacheManager();
		manager.setCaches(Arrays.asList(
				// Brukes for caching av allerede parsede og validerte graphQL queries.
				// Se https://www.graphql-java.com/documentation/v11/execution/
				new CaffeineCache(GRAPHQL_QUERY_CACHE, Caffeine.newBuilder()
						.initialCapacity(0)
						.maximumSize(0)
						.build()),
				new CaffeineCache(SAKER_BY_AKTOER_ID_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(0)
						.build()),
				new CaffeineCache(SAKER_BY_ORG_NR_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(0)
						.build()),
				new CaffeineCache(HENT_JOURNALPOSTBULK_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(0)
						.build()),
				new CaffeineCache(TILGANGSMODELL_REPO_BRUKER_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(0)
						.build()),
				new CaffeineCache(PENSJON_SAK_SAMMENDRAG_LISTE_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(0)
						.build()),
				new CaffeineCache(SAK_BY_SAKID_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(0)
						.build()),
				new CaffeineCache(SAKER_BY_FAGSAK_ID_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(0)
						.build())
		));
		return manager;
	}

}
