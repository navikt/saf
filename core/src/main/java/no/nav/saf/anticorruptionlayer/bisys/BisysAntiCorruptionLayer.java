package no.nav.saf.anticorruptionlayer.bisys;

import no.nav.saf.domain.tilgangsmodell.TilgangRelevantTredjepart;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface BisysAntiCorruptionLayer {
	List<TilgangRelevantTredjepart> hentRelevanteTredjeparter(String sakId);
}
