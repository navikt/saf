package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.nav.tilgangsmaskinen.TilgangsmaskinenConsumer;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangIdent;
import no.nav.saf.domain.tilgangsmodell.TilgangRelevantTredjepart;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;

import static no.nav.saf.domain.DomainConstants.FAGSAKSYSTEM_BISYS;
import static no.nav.saf.domain.DomainConstants.PEP3;
import static no.nav.saf.domain.kode.Tema.BID;
import static no.nav.saf.domain.kode.Tema.FAR;
import static no.nav.saf.tilgangskontroll.pep.PepAnswer.permit;

@Slf4j
@Component(PEP3)
public class TilgangsmaskinenBackedPep3Impl extends StandardTilgangsmaskinenBackedPep<TilgangSak> {

	private static final EnumSet<Tema> RELEVANTE_TEMA = EnumSet.of(BID, FAR);
	private final TilgangsmaskinenConsumer tilgangsmaskinenConsumer;

	public TilgangsmaskinenBackedPep3Impl(TilgangsmaskinenConsumer tilgangsmaskinenConsumer) {
		this.tilgangsmaskinenConsumer = tilgangsmaskinenConsumer;
	}

	@Override
	PepAnswer verifyNavIdentAccessToUser(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.warn("Pep3: Ressurs er null. Tilgang gis.");
			return PepAnswer.permit();
		}

		if (RELEVANTE_TEMA.contains(ressurs.getTema()) && FAGSAKSYSTEM_BISYS.equals(ressurs.getFagsaksystem())) {

			if (ressurs.getRelevanteTredjeparter() == null || ressurs.getRelevanteTredjeparter().isEmpty()) {
				log.info("Pep3(relevante-parter) har ingen relevante parter. Tilgang gis.");
				return PepAnswer.permit();
			}

			List<String> tredjepartIdenter = ressurs.getRelevanteTredjeparter().stream()
					.map(TilgangRelevantTredjepart::getIdent)
					.map(TilgangIdent::getIdentifikator)
					.toList();

			if (tredjepartIdenter.size() == 1) {
				return tilgangsmaskinenConsumer.navIdentHasAccess(tredjepartIdenter.getFirst(), safRequestContext, PEP3);
			} else {
				return tilgangsmaskinenConsumer.navIdentHasAccessBulk(tredjepartIdenter, safRequestContext, PEP3);
			}
		}

		return PepAnswer.permit();
	}


	@Override
	public PepAnswer verifyAzureClientCredentialFlowAccess(TilgangSak ressurs, SafRequestContext safRequestContext) {
		return permit();
	}
}
