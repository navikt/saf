package no.nav.saf.anticorruptionlayer.bisys;

import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.BidragSak;

public interface BisysAntiCorruptionLayer {
	BidragSak hentBidragSakByArkivsak(Arkivsak arkivsak);
}
