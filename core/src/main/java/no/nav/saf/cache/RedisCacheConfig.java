package no.nav.saf.cache;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.config.SafProperties;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.cache.interceptor.LoggingCacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Collections;

import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

@Configuration
@EnableCaching
@Slf4j
public class RedisCacheConfig implements CachingConfigurer {
	public static final String CACHE_MANAGER_REDIS = "redisCacheManager";
	public static final String REDIS_DOKUMENT_TILGANG_CACHE = "tilgang";
	private static final Duration DEFAULT_TTL = Duration.ofHours(1L);

	@Bean
	@Qualifier(CACHE_MANAGER_REDIS)
	CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
		// Tilgang cache brukt av Pep2d
		RedisCacheConfiguration tilgangCache = RedisCacheConfiguration.defaultCacheConfig()
				.disableCachingNullValues()
				.entryTtl(DEFAULT_TTL)
				.serializeKeysWith(fromSerializer(new StringRedisSerializer()))
				.serializeValuesWith(fromSerializer(new Jackson2JsonRedisSerializer<>(XacmlResponse.class)));

		return RedisCacheManager.builder(connectionFactory)
				.withInitialCacheConfigurations(
						Collections.singletonMap(REDIS_DOKUMENT_TILGANG_CACHE, tilgangCache))
				.enableStatistics()
				.build();
	}

	@Bean
	public RedisConnectionFactory redisConnectionFactory(SafProperties safProperties) {
		log.info("Starting redis connection on {}", safProperties.getRedis());
		return new LettuceConnectionFactory(new RedisStandaloneConfiguration(safProperties.getRedis().getHostname(), safProperties.getRedis().getPortnumber()));
	}

	@Override
	public CacheErrorHandler errorHandler() {
		return new LoggingCacheErrorHandler();
	}
}