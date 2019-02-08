package no.nav.saf.anticorruptionlayer.bisys;

import no.nav.saf.domain.BidragSak;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface BisysAntiCorruptionLayer {

	BidragSak hentBidragSak(String sakId);
}
