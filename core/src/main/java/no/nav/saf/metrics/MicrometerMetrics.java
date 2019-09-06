package no.nav.saf.metrics;

import io.micrometer.core.instrument.Counter;
import lombok.experimental.UtilityClass;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@UtilityClass
public final class MicrometerMetrics {
	public static final Counter.Builder CACHE_GETS_BUILDER = Counter.builder("cache_gets")
			.description("Cache gets for policy enforcement points")
			.tags("cacheManager", "redis");
}
