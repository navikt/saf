package no.nav.saf.coordinator;

import com.github.javafaker.Faker;
import no.nav.saf.context.mock.MockData;
import no.nav.saf.context.saf.domain.Journalpost;
import no.nav.saf.context.saf.domain.Sak;
import no.nav.saf.context.saf.domain.Tema;
import no.nav.saf.context.saf.domain.kode.Temakode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
		// mockdata
		return Stream.of(
				Tema.fromTemakode(Temakode.BID),
				Tema.fromTemakode(Temakode.FOR)
		).filter(t -> temakoder.isEmpty() || temakoder.contains(t.getTema())).collect(Collectors.toSet());
	}

	@Override
	public List<Sak> findSakerByAktoerIdAndTema(String aktoerId, Temakode tema) {
		// mockdata
		Faker faker = new Faker();
		switch(tema) {
			case BID:
				return MockData.bidragsaker(faker);
			case FOR:
				return MockData.foreldrepengesaker(faker);
			default:
				return new ArrayList<>();
		}
	}

	@Override
	public List<Journalpost> findJournalposterByFagsakAndTema(String fagsaksnummer, Temakode tema) {
		// mockdata
		Faker faker = new Faker();
		switch (tema) {
			case BID:
				return MockData.bidragjournalposter(faker);
			case FOR:
				return MockData.foreldrepengerjournalposter(faker);
			default:
				return new ArrayList<>();
		}
	}
}
