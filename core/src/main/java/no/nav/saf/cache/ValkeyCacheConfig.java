package no.nav.saf.cache;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.LoggingCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import static java.util.Collections.singletonMap;
import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

@Configuration
@EnableCaching
@Slf4j
public class ValkeyCacheConfig implements CachingConfigurer {
	public static final String VALKEY_CACHE_MANAGER = "valkeyCacheManager";
	public static final String VALKEY_DOKUMENT_TILGANG_CACHE = "dokument-tilgang";
	private final Environment environment;

	public ValkeyCacheConfig(Environment environment) {
		this.environment = environment;
	}

	@Bean
	@Qualifier(VALKEY_CACHE_MANAGER)
	CacheManager valkeyCacheManager(RedisConnectionFactory connectionFactory) {
		return RedisCacheManager.builder(connectionFactory)
				.withInitialCacheConfigurations(
						singletonMap(VALKEY_DOKUMENT_TILGANG_CACHE, valkeyDokumentTilgangCacheConfiguration()))
				.enableStatistics()
				.build();
	}

	private RedisCacheConfiguration valkeyDokumentTilgangCacheConfiguration() {
		return RedisCacheConfiguration.defaultCacheConfig()
				.disableCachingNullValues()
				.serializeValuesWith(fromSerializer(new Jackson2JsonRedisSerializer<>(XacmlResponse.class)))
				// En valkey-app håndterer alle testmiljøene
				.prefixCacheNameWith(environment.getProperty("nais.app.name", "saf") + "-");
	}

	@Override
	public CacheErrorHandler errorHandler() {
		return new LoggingCacheErrorHandler();
	}
}