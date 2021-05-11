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

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
@EnableCaching
public class LokalCacheConfig {
	public static final String GRAPHQL_QUERY_CACHE = "graphQLQuery";
	public static final String BIDRAG_SAK_BY_SAKID_CACHE = "bidrakSakerBySakId";
	public static final String HENT_JOURNALPOSTBULK_CACHE = "hentTilgangJournalposterBulk";
	public static final String TILGANGSMODELL_REPO_BRUKER_CACHE = "tilgangsmodellRepoBruker";
	public static final String PENSJON_SAK_SAMMENDRAG_LISTE_CACHE = "pensjonSakSammendragListe";
	public static final String REST_STS_CACHE = "RESTSTS";

	@Bean
	@Primary
	@Profile({"nais", "local"})
	CacheManager cacheManager() {
		SimpleCacheManager manager = new SimpleCacheManager();
		manager.setCaches(Arrays.asList(
				// Lokale caches
				new CaffeineCache(TILGANGSMODELL_REPO_BRUKER_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(500)
						.build()),
				new CaffeineCache(PENSJON_SAK_SAMMENDRAG_LISTE_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(500)
						.build()),
				new CaffeineCache(BIDRAG_SAK_BY_SAKID_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(500)
						.build()),
				new CaffeineCache(REST_STS_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(50, TimeUnit.MINUTES)
						.maximumSize(1)
						.build())
		));
		return manager;
	}
}
