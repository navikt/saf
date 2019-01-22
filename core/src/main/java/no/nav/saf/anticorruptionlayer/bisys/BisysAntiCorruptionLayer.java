package no.nav.saf.anticorruptionlayer.bisys;

import no.nav.saf.domain.BidragSak;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface BisysAntiCorruptionLayer {

	BidragSak hentBidragSak(String sakId, TilgangBruker tilgangBruker);
}
