package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static no.nav.saf.domain.DomainConstants.PEP1G;

@Slf4j
@Component(PEP1G)
public class MultiPep1g extends AbstractMultiPep<TilgangBruker> {

	public MultiPep1g(AbacBackedPep1gImpl abacBackedPep,
					  TilgangsmaskinenBackedPep1gImpl tilgangsmaskinenBackedPep,
					  @Value("${saf.pep1g.prioritize_tilgangsmaskinen}") boolean prioritizeTilgangsmaskinenAnswer) {
		super(abacBackedPep, tilgangsmaskinenBackedPep, prioritizeTilgangsmaskinenAnswer, PEP1G);
	}
}

