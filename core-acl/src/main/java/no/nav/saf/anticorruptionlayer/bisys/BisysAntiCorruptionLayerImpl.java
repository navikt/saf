package no.nav.saf.anticorruptionlayer.bisys;

import no.nav.saf.domain.tilgangsmodell.TilgangRelevantTredjepart;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class BisysAntiCorruptionLayerImpl implements BisysAntiCorruptionLayer {
	@Override
	public List<TilgangRelevantTredjepart> hentRelevanteTredjeparter(String sakId) {
		// TODO MMA-1058
		return new ArrayList<>();
	}
}
