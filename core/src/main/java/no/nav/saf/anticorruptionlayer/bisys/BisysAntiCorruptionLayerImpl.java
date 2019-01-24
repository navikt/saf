package no.nav.saf.anticorruptionlayer.bisys;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.bisys.hentbidragsak.BidragSakConsumer;
import no.nav.saf.anticorruptionlayer.bisys.hentbidragsak.BidragSakTo;
import no.nav.saf.domain.BidragSak;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangIdent;
import no.nav.saf.domain.tilgangsmodell.TilgangRelevantTredjepart;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.stream.Collectors;

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
	public BidragSak hentBidragSak(String sakId, TilgangBruker tilgangBruker) {
		try {
			final BidragSakTo bidragSakTo = bidragSakConsumer.hentBidragSak(sakId);
			return BidragSak.builder()
					.paragraf19(bidragSakTo.isErParagraf19())
					.relevanteTredjeparter(
							bidragSakTo.getRoller().stream()
									.filter(fnrRolle -> isRolleBruker(fnrRolle, tilgangBruker))
									.map(fnrRolle -> new TilgangRelevantTredjepart(TilgangIdent.builder()
											.identifikator(fnrRolle)
											.build()))
									.collect(Collectors.toList()))
					.build();
		} catch (Exception e) {
			log.warn("Kunne ikke hente relevante tredjeparter og paragraf19 fra bidrag for sakId={}", sakId, e);
			return null;
		}
	}

	/**
	 * * Bidrag returnerer fnr til alle som har en rolle i saken, inkl. brukeren. Bruker må derfor filtreres bort
	 */
	private boolean isRolleBruker(String fnrRolle, TilgangBruker tilgangBruker) {
		if (fnrRolle == null || tilgangBruker == null) {
			return false;
		} else {
			return fnrRolle.equals(tilgangBruker.getFoedselsnr());
		}
	}
}
