package no.nav.saf.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Configuration
@EnableCaching
public class LokalCacheConfig {

	public static final String GRAPHQL_QUERY_CACHE = "graphQLQuery";
	public static final String SAKER_BY_AKTOER_ID_CACHE = "sakerByAktoerId";
	public static final String SAKER_BY_FAGSAK_ID_CACHE = "sakerByFagsakId";
	public static final String SAKER_BY_ORG_NR_CACHE = "sakerByOrgNr";
	public static final String SAK_BY_SAKID_CACHE = "sakerBySakId";
	public static final String HENT_JOURNALPOSTBULK_CACHE = "hentTilgangJournalposterBulk";
	public static final String TILGANGSMODELL_REPO_BRUKER_CACHE = "tilgangsmodellRepoBruker";
	public static final String PENSJON_SAK_SAMMENDRAG_LISTE_CACHE = "pensjonSakSammendragListe";
	public static final String TILGANGSMODELL_REPO_SAK_CACHE = "tilgangsmodellRepoSak";
	public static final String HENT_TILGANG_JOURNALPOST_CACHE = "hentTilgangJournalpost";

	@Bean
	@Primary
	CacheManager cacheManager() {
		SimpleCacheManager manager = new SimpleCacheManager();
		manager.setCaches(Arrays.asList(
				// Brukes for caching av allerede parsede og validerte graphQL queries.
				// Se https://www.graphql-java.com/documentation/v11/execution/
				new CaffeineCache(GRAPHQL_QUERY_CACHE, Caffeine.newBuilder()
						.initialCapacity(50)
						.maximumSize(5_000)
						.build()),
				new CaffeineCache(SAKER_BY_AKTOER_ID_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(500)
						.build()),
				new CaffeineCache(SAKER_BY_ORG_NR_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(500)
						.build()),
				new CaffeineCache(HENT_JOURNALPOSTBULK_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(500)
						.build()),
				new CaffeineCache(TILGANGSMODELL_REPO_BRUKER_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(500)
						.build()),
				new CaffeineCache(PENSJON_SAK_SAMMENDRAG_LISTE_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(500)
						.build()),
				new CaffeineCache(TILGANGSMODELL_REPO_SAK_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(500)
						.build()),
				new CaffeineCache(HENT_TILGANG_JOURNALPOST_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(500)
						.build()),
				new CaffeineCache(SAK_BY_SAKID_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(500)
						.build()),
				new CaffeineCache(SAKER_BY_FAGSAK_ID_CACHE, Caffeine.newBuilder()
						.expireAfterWrite(10, TimeUnit.MINUTES)
						.maximumSize(500)
						.build())
		));
		return manager;
	}
}
