package no.nav.saf.anticorruptionlayer.bisys;

import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.BidragSak;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface BisysAntiCorruptionLayer {
	BidragSak hentBidragSakByArkivsak(Arkivsak arkivsak);
}
