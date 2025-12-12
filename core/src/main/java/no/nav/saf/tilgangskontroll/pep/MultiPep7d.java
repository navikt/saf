package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static no.nav.saf.domain.DomainConstants.PEP7D;

@Slf4j
@Component(PEP7D)
public class MultiPep7d extends AbstractMultiPep<TilgangSak> {

	public MultiPep7d(AbacBackedPep7dImpl abacBackedPep,
					  TilgangsmaskinenBackedPep7dImpl tilgangsmaskinenBackedPep,
					  @Value("${saf.pep7d.prioritize_tilgangsmaskinen}") boolean prioritizeTilgangsmaskinenAnswer) {
		super(abacBackedPep, tilgangsmaskinenBackedPep, prioritizeTilgangsmaskinenAnswer, PEP7D);
	}
}
