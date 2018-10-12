package no.nav.saf.coordinator;

import no.nav.saf.context.saf.domain.Tema;
import no.nav.saf.context.saf.domain.kode.Temakode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
class SakstilknyttedeJournalposterCoordinatorImpl implements SakstilknyttedeJournalposterCoordinator {

	@Override
	public Set<Tema> findTemaKnyttetTilAktoerIdAndFilterByTemakoder(final String aktoerId, final List<Temakode> temakoder) {
		return Stream.of(
				Tema.fromTemakode(Temakode.BID),
				Tema.fromTemakode(Temakode.FOR)
		).filter(t -> temakoder.isEmpty() || temakoder.contains(t.getTema())).collect(Collectors.toSet());
	}
}
