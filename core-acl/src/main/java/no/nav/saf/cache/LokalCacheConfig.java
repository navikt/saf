package no.nav.saf.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
@EnableCaching
public class LokalCacheConfig {

	public static final String SAKER_BY_AKTOER_ID_CACHE = "sakerByAktoerId";
	public static final String HENT_JOURNALPOSTER_CACHE = "hentJournalposter";
	public static final String TILGANGSMODELL_REPO_BRUKER_CACHE = "tilgangsmodellRepoBruker";
	public static final String TILGANGSMODELL_REPO_SAK_CACHE = "tilgangsmodellRepoSak";
	public static final String TILGANGSMODELL_REPO_JORNALPOST_CACHE = "tilgangsmodellRepoJournalpost";


	@Bean
	CacheManager cacheManager() {
		SimpleCacheManager manager = new SimpleCacheManager();
		manager.setCaches(Arrays.asList(
				new CaffeineCache(SAKER_BY_AKTOER_ID_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(500)
						.build()),
				new CaffeineCache(HENT_JOURNALPOSTER_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(500)
						.build()),
				new CaffeineCache(TILGANGSMODELL_REPO_BRUKER_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(500)
						.build()),
				new CaffeineCache(TILGANGSMODELL_REPO_SAK_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(500)
						.build()),
				new CaffeineCache(TILGANGSMODELL_REPO_JORNALPOST_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(500)
						.build())
		));
		return manager;
	}
}
