package no.nav.saf.domain;

import no.nav.saf.anticorruptionlayer.aktoerid.AktoerAntiCorruptionLayer;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Repository
public class TilgangsmodellRepositoryImpl implements TilgangsmodellRepository {
	private final AktoerAntiCorruptionLayer aktoerAntiCorruptionLayer;

	@Inject
	public TilgangsmodellRepositoryImpl(AktoerAntiCorruptionLayer aktoerAntiCorruptionLayer) {
		this.aktoerAntiCorruptionLayer = aktoerAntiCorruptionLayer;
	}

	@Override
	public TilgangBruker findTilgangBrukerByAktoerId(String aktoerId) {
		//TODO MMA-1122
		return null;
	}
}
