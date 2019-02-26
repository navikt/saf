package no.nav.saf.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
public class OnlyLoggingCacheErrorHandler implements CacheErrorHandler {
	@Override
	public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
		log.warn(String.format("Feil ved Cache Get operasjon. CacheNavn=%s, feilklasse=%s, feilmelding=%s", cache.getName(), exception
				.getClass()
				.getSimpleName(), exception.getMessage()));
	}

	@Override
	public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
		log.warn(String.format("Feil ved Cache Put operasjon. CacheNavn=%s, feilklasse=%s, feilmelding=%s", cache.getName(), exception
				.getClass()
				.getSimpleName(), exception.getMessage()));
	}

	@Override
	public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
		log.warn(String.format("Feil ved Cache Evict operasjon. CacheNavn=%s, feilklasse=%s, feilmelding=%s", cache.getName(), exception
				.getClass()
				.getSimpleName(), exception.getMessage()));
	}

	@Override
	public void handleCacheClearError(RuntimeException exception, Cache cache) {
		log.warn(String.format("Feil ved Cache Clear operasjon. CacheNavn=%s, feilklasse=%s, feilmelding=%s", cache.getName(), exception
				.getClass()
				.getSimpleName(), exception.getMessage()));
	}
}
