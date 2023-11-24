package no.nav.saf;

import io.micrometer.context.ContextRegistry;
import no.nav.saf.util.MDCConstants;
import org.slf4j.MDC;
import org.springframework.boot.actuate.metrics.cache.CacheMetricsRegistrar;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.cache.CacheManager;
import org.springframework.context.ApplicationListener;
import reactor.core.publisher.Hooks;

public class ApplicationStartedEventListener implements ApplicationListener<ApplicationStartedEvent> {

	@Override
	public void onApplicationEvent(ApplicationStartedEvent event) {
		registerCacheMetrics(event);
		registerReactorContextPropagation();
	}

	private static void registerReactorContextPropagation() {
		Hooks.enableAutomaticContextPropagation();
		MDCConstants.ALL_KEYS.forEach(ApplicationStartedEventListener::registerMDCKey);
	}

	private static void registerMDCKey(String key) {
		ContextRegistry.getInstance().registerThreadLocalAccessor(
				key,
				() -> MDC.get(key),
				value -> MDC.put(key, value),
				() -> MDC.remove(key));
	}

	private static void registerCacheMetrics(ApplicationStartedEvent event) {
		CacheMetricsRegistrar cacheMetricsRegistrar = event.getApplicationContext().getBean(CacheMetricsRegistrar.class);
		CacheManager cacheManager = event.getApplicationContext().getBean(CacheManager.class);

		cacheManager.getCacheNames().forEach(cacheName -> cacheMetricsRegistrar.bindCacheToRegistry(cacheManager.getCache(cacheName)));
	}
}
