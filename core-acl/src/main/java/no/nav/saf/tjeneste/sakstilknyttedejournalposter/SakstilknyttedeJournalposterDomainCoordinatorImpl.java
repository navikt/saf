package no.nav.saf.tjeneste.sakstilknyttedejournalposter;

import no.nav.saf.domain.TilgangsmodellRepository;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.PepEvaluator;
import no.nav.saf.tjeneste.visningsmodell.Bruker;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Skiller boblekontekst for SAF domenet fra legacy kontekster.
 * Koordinerer tilgangskontroll og filtrerer visningsmodell basert på resultater fra denne.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
class SakstilknyttedeJournalposterDomainCoordinatorImpl implements SakstilknyttedeJournalposterDomainCoordinator {

	private final PepEvaluator<TilgangBruker> pepEvaluator1;
	private final TilgangsmodellRepository tilgangsmodellRepository;
	private final SakstilknyttedeJournalposterVisningsmodellRepository sakstilknyttedeJournalposterVisningsmodellRepository;

	@Inject
	SakstilknyttedeJournalposterDomainCoordinatorImpl(@Named("pep1") PepEvaluator<TilgangBruker> pepEvaluator1,
													  TilgangsmodellRepository tilgangsmodellRepository,
													  SakstilknyttedeJournalposterVisningsmodellRepository sakstilknyttedeJournalposterVisningsmodellRepository) {
		this.pepEvaluator1 = pepEvaluator1;
		this.tilgangsmodellRepository = tilgangsmodellRepository;
		this.sakstilknyttedeJournalposterVisningsmodellRepository = sakstilknyttedeJournalposterVisningsmodellRepository;
	}

	@Override
	public Bruker findBrukerByAktoerId(String aktoerId, SafRequestContext safRequestContext) {
		// TODO MMA-1121

		return Bruker.builder().aktoerId(aktoerId).build();
	}
}
