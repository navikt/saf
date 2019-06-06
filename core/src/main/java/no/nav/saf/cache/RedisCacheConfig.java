package no.nav.saf.cache;

import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import javax.inject.Named;
import java.time.Duration;
import java.util.Collections;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
@EnableCaching
public class RedisCacheConfig extends CachingConfigurerSupport {
	public static final String MANAGER_DISTRIBUTED = "distributed";
	// Ikke endre denne verdien, en del av NAIS redis oppsett
	private static final String MASTER_NAME = "mymaster";
	private static final Duration DEFAULT_TTL = Duration.ofHours(1L);
	public static final String TILGANG_CACHE = "tilgang";

	@Bean
	@Named(MANAGER_DISTRIBUTED)
	CacheManager redisCacheManager(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
		// Tilgang cache brukt av Pep2d
		RedisCacheConfiguration tilgangCache = RedisCacheConfiguration.defaultCacheConfig()
				.disableCachingNullValues()
				.entryTtl(DEFAULT_TTL)
				.serializeKeysWith(fromSerializer(new StringRedisSerializer()))
				.serializeValuesWith(fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)));

		return RedisCacheManager.builder(connectionFactory)
				.withInitialCacheConfigurations(
						Collections.singletonMap(TILGANG_CACHE, tilgangCache))
				.build();
	}

	@Bean
	public RedisConnectionFactory redisConnectionFactory(@Value("${REDIS_HOST:rfs-saf}") String redisHost,
														 LettuceClientConfiguration clientConfiguration) {
		LettuceConnectionFactory factory = new LettuceConnectionFactory(
				new RedisSentinelConfiguration().master(MASTER_NAME).sentinel(new RedisNode(redisHost, 26379)),
				clientConfiguration);
		factory.setShareNativeConnection(true);
		return factory;
	}

	@Bean
	public LettuceClientConfiguration poolingClientConfiguration() {
		return LettucePoolingClientConfiguration.builder()
				.poolConfig(getPoolConfig())
				.clientOptions(ClientOptions.builder()
						.autoReconnect(true)
						.cancelCommandsOnReconnectFailure(true)
						.pingBeforeActivateConnection(true)
						.disconnectedBehavior(ClientOptions.DisconnectedBehavior.REJECT_COMMANDS)
						.suspendReconnectOnProtocolFailure(false)
						.socketOptions(SocketOptions.builder().connectTimeout(Duration.ofMillis(500)).build())
						.build())
				.build();
	}

	private GenericObjectPoolConfig<?> getPoolConfig() {
		GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
		genericObjectPoolConfig.setTestOnReturn(false);
		genericObjectPoolConfig.setTestOnCreate(false);
		genericObjectPoolConfig.setTestWhileIdle(false);
		genericObjectPoolConfig.setTestOnBorrow(false);
		genericObjectPoolConfig.setMaxTotal(16);
		genericObjectPoolConfig.setMaxIdle(8);
		genericObjectPoolConfig.setMinIdle(8);
		genericObjectPoolConfig.setTimeBetweenEvictionRunsMillis(10000);
		genericObjectPoolConfig.setMinEvictableIdleTimeMillis(6000);
		return genericObjectPoolConfig;
	}

	@Bean
	@Override
	public CacheErrorHandler errorHandler() {
		return new OnlyLoggingCacheErrorHandler();
	}
}
