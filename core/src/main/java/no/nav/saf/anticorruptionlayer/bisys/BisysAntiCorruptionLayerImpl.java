package no.nav.saf.anticorruptionlayer.bisys;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.bisys.hentbidragsak.BidragSakConsumer;
import no.nav.saf.anticorruptionlayer.bisys.hentbidragsak.BidragSakTo;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.BidragSak;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangIdent;
import no.nav.saf.domain.tilgangsmodell.TilgangRelevantTredjepart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static no.nav.saf.domain.DomainConstants.FAGSAKSYSTEM_BISYS;
import static no.nav.saf.domain.kode.Tema.BID;
import static no.nav.saf.domain.kode.Tema.FAR;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
class BisysAntiCorruptionLayerImpl implements BisysAntiCorruptionLayer {

	private final BidragSakConsumer bidragSakConsumer;

	@Autowired
	public BisysAntiCorruptionLayerImpl(BidragSakConsumer bidragSakConsumer) {
		this.bidragSakConsumer = bidragSakConsumer;
	}

	private final List<Tema> relevanteTema = Arrays.asList(BID, FAR);

	@Override
	public BidragSak hentBidragSakByArkivsak(Arkivsak arkivsak) {
		if (relevanteTema.contains(arkivsak.getTema()) && FAGSAKSYSTEM_BISYS.equals(arkivsak.getFagsaksystem())) {
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
