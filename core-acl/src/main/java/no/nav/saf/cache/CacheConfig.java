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
public class CacheConfig {

	public static final String SAKER_BY_AKTOER_ID_CACHE = "sakerByAktoerId";
	public static final String HENT_JOURNALPOSTER_CACHE = "hentJournalposter";

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
						.build())
		));
		return manager;
	}
}
