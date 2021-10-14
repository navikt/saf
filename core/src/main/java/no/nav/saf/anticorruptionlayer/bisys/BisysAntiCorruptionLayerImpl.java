package no.nav.saf.anticorruptionlayer.bisys;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.bisys.hentbidragsak.BidragSakConsumer;
import no.nav.saf.anticorruptionlayer.bisys.hentbidragsak.BidragSakTo;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.BidragSak;
import no.nav.saf.domain.tilgangsmodell.TilgangIdent;
import no.nav.saf.domain.tilgangsmodell.TilgangRelevantTredjepart;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.stream.Collectors;

import static no.nav.saf.domain.DomainConstants.FAGSAKSYSTEM_BISYS;
import static no.nav.saf.domain.kode.Tema.BID;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
class BisysAntiCorruptionLayerImpl implements BisysAntiCorruptionLayer {

	private final BidragSakConsumer bidragSakConsumer;

	@Inject
	public BisysAntiCorruptionLayerImpl(BidragSakConsumer bidragSakConsumer) {
		this.bidragSakConsumer = bidragSakConsumer;
	}

	@Override
	public BidragSak hentBidragSakByArkivsak(Arkivsak arkivsak) {
		if (BID.equals(arkivsak.getTema()) || FAGSAKSYSTEM_BISYS.equals(arkivsak.getFagsaksystem())) { // TODO: Denne burde vere &&
			return hentBidragSak(arkivsak.getFagsakId());
		}
		return new BidragSak();
	}

	public BidragSak hentBidragSak(String sakId) {
		try {
			final BidragSakTo bidragSakTo = bidragSakConsumer.hentBidragSak(sakId);
			return BidragSak.builder()
					.relevanteTredjeparter(
							bidragSakTo.getRoller().stream()
									.map(fnrRolle -> new TilgangRelevantTredjepart(TilgangIdent.builder()
											.identifikator(fnrRolle)
											.build()))
									.collect(Collectors.toList()))
					.build();
		} catch (Exception e) {
			log.warn("Kunne ikke hente relevante tredjeparter fra bidrag for sakId={}", sakId, e);
			return null;
		}
	}
}
