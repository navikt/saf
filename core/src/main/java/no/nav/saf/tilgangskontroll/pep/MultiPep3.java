package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static no.nav.saf.domain.DomainConstants.PEP3;

@Slf4j
@Component(PEP3)
public class MultiPep3 extends AbstractMultiPep<TilgangSak> {

	public MultiPep3(AbacBackedPep3Impl abacBackedPep,
					 TilgangsmaskinenBackedPep3Impl tilgangsmaskinenBackedPep,
					 @Value("${saf.pep3.prioritize_tilgangsmaskinen}") boolean prioritizeTilgangsmaskinenAnswer) {
		super(abacBackedPep, tilgangsmaskinenBackedPep, prioritizeTilgangsmaskinenAnswer, PEP3);
	}
}
