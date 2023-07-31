package no.nav.saf.cache;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.config.SafProperties;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Collections;

import static java.time.Duration.ofSeconds;
import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

@Configuration
@EnableCaching
@Slf4j
public class RedisCacheConfig implements CachingConfigurer {
	public static final String MANAGER_DISTRIBUTED = "distributed";
	private static final Duration DEFAULT_TTL = Duration.ofHours(1L);
	public static final String TILGANG_CACHE = "tilgang";

	@Bean
	@Qualifier(MANAGER_DISTRIBUTED)
	CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
		// Tilgang cache brukt av Pep2d
		RedisCacheConfiguration tilgangCache = RedisCacheConfiguration.defaultCacheConfig()
				.disableCachingNullValues()
				.entryTtl(DEFAULT_TTL)
				.serializeKeysWith(fromSerializer(new StringRedisSerializer()))
				.serializeValuesWith(fromSerializer(new Jackson2JsonRedisSerializer<>(XacmlResponse.class)));

		return RedisCacheManager.builder(connectionFactory)
				.withInitialCacheConfigurations(
						Collections.singletonMap(TILGANG_CACHE, tilgangCache))
				.build();
	}

	@Bean
	public RedisConnectionFactory redisConnectionFactory(SafProperties safProperties,
														 LettuceClientConfiguration clientConfiguration) {
		SafProperties.Redis redisConfig = safProperties.getRedis();
		RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
		log.info("Starting redis connection on {}", redisConfig);
		config.setHostName(redisConfig.getHostname());
		config.setPort(redisConfig.getPortnumber());
		LettuceConnectionFactory factory = new LettuceConnectionFactory(config, clientConfiguration);
		factory.setShareNativeConnection(true);
		return factory;
	}

	@Bean
	public LettuceClientConfiguration poolingClientConfiguration() {
		return LettucePoolingClientConfiguration.builder()
				.poolConfig(getPoolConfig())
				.clientOptions(ClientOptions.builder()
						.autoReconnect(true)
						.pingBeforeActivateConnection(true)
						.disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
						.suspendReconnectOnProtocolFailure(false)
						.socketOptions(SocketOptions.builder().connectTimeout(Duration.ofMillis(500)).build())
						.build())
				.build();
	}

	private GenericObjectPoolConfig<?> getPoolConfig() {
		GenericObjectPoolConfig<?> genericObjectPoolConfig = new GenericObjectPoolConfig<>();
		genericObjectPoolConfig.setTestOnReturn(false);
		genericObjectPoolConfig.setTestOnCreate(false);
		genericObjectPoolConfig.setTestWhileIdle(false);
		genericObjectPoolConfig.setTestOnBorrow(false);
		genericObjectPoolConfig.setMaxTotal(16);
		genericObjectPoolConfig.setMaxIdle(8);
		genericObjectPoolConfig.setMinIdle(8);
		genericObjectPoolConfig.setTimeBetweenEvictionRuns(ofSeconds(10));
		genericObjectPoolConfig.setMinEvictableIdleTime(ofSeconds(6));
		return genericObjectPoolConfig;
	}

	@Bean
	@Override
	public CacheErrorHandler errorHandler() {
		return new OnlyLoggingCacheErrorHandler();
	}
}
