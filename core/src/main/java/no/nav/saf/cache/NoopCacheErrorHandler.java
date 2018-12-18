package no.nav.saf.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class NoopCacheErrorHandler implements CacheErrorHandler {
	@Override
	public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
		//noop
	}

	@Override
	public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
		//noop
	}

	@Override
	public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
		//noop
	}

	@Override
	public void handleCacheClearError(RuntimeException exception, Cache cache) {
		//noop
	}
}
