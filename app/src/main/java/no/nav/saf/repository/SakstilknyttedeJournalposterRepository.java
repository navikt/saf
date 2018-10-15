package no.nav.saf.repository;

import no.nav.saf.context.saf.domain.Tema;
import no.nav.saf.context.saf.domain.kode.Temakode;
import no.nav.saf.coordinator.SakstilknyttedeJournalposterCoordinator;
import no.nav.saf.context.saf.domain.Bruker;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Repository
public class SakstilknyttedeJournalposterRepository {
	private final SakstilknyttedeJournalposterCoordinator coordinator;

	@Inject
	public SakstilknyttedeJournalposterRepository(SakstilknyttedeJournalposterCoordinator coordinator) {
		this.coordinator = coordinator;
	}

	public Bruker findBrukerByAktoerId(final String aktoerId) {
		return Bruker.builder().aktoerId(aktoerId).build();
	}

	public Set<Tema> findTemaKnyttetTilAktoerIdAndFilterByTemakoder(final String aktoerId, final List<Temakode> temakoder) {
		return coordinator.findTemaKnyttetTilAktoerIdAndFilterByTemakoder(aktoerId, temakoder);
	}
}
