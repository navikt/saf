package no.nav.saf.domain;

import static java.lang.String.format;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.aktoerid.AktoerAntiCorruptionLayer;
import no.nav.saf.cache.LokalCacheConfig;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Repository
@Slf4j
public class TilgangsmodellRepositoryImpl implements TilgangsmodellRepository {
	private final AktoerAntiCorruptionLayer aktoerAntiCorruptionLayer;

	@Inject
	public TilgangsmodellRepositoryImpl(AktoerAntiCorruptionLayer aktoerAntiCorruptionLayer) {
		this.aktoerAntiCorruptionLayer = aktoerAntiCorruptionLayer;
	}

	@Override
	@Cacheable(cacheNames = LokalCacheConfig.TILGANGSMODELL_REPO_BRUKER_CACHE)
	public TilgangBruker findTilgangBrukerByAktoerId(String aktoerId) {
		try {
			return aktoerAntiCorruptionLayer.hentTilgangBruker(aktoerId);
		} catch (Exception e) {
			log.warn(format("Feilet ved oppslag av aktoer=%s. Feilmelding=%s", aktoerId, e.getMessage()));
		}
		return null;
	}
}
