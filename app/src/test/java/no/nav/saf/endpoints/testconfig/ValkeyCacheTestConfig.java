package no.nav.saf.endpoints.testconfig;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import redis.embedded.RedisCluster;

import java.io.IOException;

@TestConfiguration
@Profile("itest")
public class ValkeyCacheTestConfig {

	private final RedisCluster redisCluster;

	public ValkeyCacheTestConfig() throws IOException {
		this.redisCluster = RedisCluster.newRedisCluster()
				.ephemeral()
				.replicationGroup("m1", 0)
				.build();
	}

	@PostConstruct
	public void postConstruct() throws IOException, InterruptedException {
		this.redisCluster.start();
	}

	@PreDestroy
	public void preDestroy() throws IOException {
		this.redisCluster.stop();
	}

	@Primary
	@Bean
	public LettuceConnectionFactory redisConnectionFactory() {
		return new LettuceConnectionFactory(
				"localhost",
				redisCluster.serverPorts().getFirst());
	}
}
