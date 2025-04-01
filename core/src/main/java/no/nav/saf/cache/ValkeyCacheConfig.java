package no.nav.saf.cache;

import java.util.Map;

import static org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair.fromSerializer;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import org.springframework.beans.factory.annotation.Qualifier;
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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
@Slf4j
public class ValkeyCacheConfig implements CachingConfigurer {
	public static final String VALKEY_CACHE_MANAGER = "valkeyCacheManager";
	public static final String VALKEY_DOKUMENT_TILGANG_CACHE = "dokument-tilgang";
	public static final String VALKEY_MSGRAPH_GRUPPER_CACHE = "msgraph-grupper";
	public static final Duration VALKEY_CACHE_ENTRY_TTL = Duration.ofHours(12);
	public final String cacheNamePrefix;

	public ValkeyCacheConfig(Environment environment) {
		cacheNamePrefix = environment.getProperty("nais.app.name", "saf") + "-";
	}

	@Bean
	@Qualifier(VALKEY_CACHE_MANAGER)
	RedisCacheManager valkeyCacheManager(RedisConnectionFactory connectionFactory) {
		return RedisCacheManager.builder(connectionFactory)
				.withInitialCacheConfigurations(
						Map.of(
								VALKEY_DOKUMENT_TILGANG_CACHE, valkeyDokumentTilgangCacheConfiguration(),
								VALKEY_MSGRAPH_GRUPPER_CACHE, valkeyMsGraphCacheConfiguration()))
				.enableStatistics()
				.build();
	}

	private RedisCacheConfiguration valkeyDokumentTilgangCacheConfiguration() {
		return RedisCacheConfiguration.defaultCacheConfig()
				.disableCachingNullValues()
				.serializeValuesWith(fromSerializer(new Jackson2JsonRedisSerializer<>(XacmlResponse.class)))
				// En valkey-app håndterer alle testmiljøene
				.prefixCacheNameWith(cacheNamePrefix)
				.entryTtl(VALKEY_CACHE_ENTRY_TTL);
	}

	private RedisCacheConfiguration valkeyMsGraphCacheConfiguration() {
		// denne brukes ikke, men er inkludert for completeness
		return RedisCacheConfiguration.defaultCacheConfig()
				.disableCachingNullValues()
				// En valkey-app håndterer alle testmiljøene
				.prefixCacheNameWith(cacheNamePrefix)
				.entryTtl(VALKEY_CACHE_ENTRY_TTL);
	}

	@Bean
	public StringRedisTemplate valkeyRedisTemplate(RedisConnectionFactory connectionFactory) {
		StringRedisTemplate template = new StringRedisTemplate();
		template.setConnectionFactory(connectionFactory);
		return template;
	}

	@Bean
	public ValkeyGrupperMedlemskapCacheConfiguration valkeyGrupperMedlemskapCacheConfiguration() {
		return new ValkeyGrupperMedlemskapCacheConfiguration(cacheNamePrefix + VALKEY_MSGRAPH_GRUPPER_CACHE + "::");
	}

	@Override
	public CacheErrorHandler errorHandler() {
		return new LoggingCacheErrorHandler();
	}

	public record ValkeyGrupperMedlemskapCacheConfiguration(String valkeyKeyPrefix) {
	}
}