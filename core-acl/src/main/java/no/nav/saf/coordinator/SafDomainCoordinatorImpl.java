package no.nav.saf.coordinator;

import no.nav.saf.domain.TilgangsmodellRepository;
import no.nav.saf.domain.VisningsmodellRepository;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.visningsmodell.Bruker;
import no.nav.saf.tilgangskontroll.pep.PepEvaluator;
import org.springframework.stereotype.Component;

import javax.inject.Inject;

/**
 * Skiller boblekontekst for SAF domenet fra legacy kontekster.
 * Koordinerer tilgangskontroll og filtrerer visningsmodell basert på resultater fra denne.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
class SafDomainCoordinatorImpl implements SafDomainCoordinator {

	private final PepEvaluator<TilgangBruker> pepEvaluator1;
	private final TilgangsmodellRepository tilgangsmodellRepository;
	private final VisningsmodellRepository visningsmodellRepository;

	@Inject
	SafDomainCoordinatorImpl(PepEvaluator<TilgangBruker> pepEvaluator1,
							 TilgangsmodellRepository tilgangsmodellRepository,
							 VisningsmodellRepository visningsmodellRepository) {
		this.pepEvaluator1 = pepEvaluator1;
		this.tilgangsmodellRepository = tilgangsmodellRepository;
		this.visningsmodellRepository = visningsmodellRepository;
	}

	@Override
	public Bruker findBrukerByAktoerId(String aktoerId) {
		// TODO MMA-1121

		return Bruker.builder().aktoerId(aktoerId).build();
	}
}
