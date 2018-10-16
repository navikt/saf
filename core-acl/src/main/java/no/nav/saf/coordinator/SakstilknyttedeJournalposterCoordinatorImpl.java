package no.nav.saf.coordinator;

import com.github.javafaker.Faker;
import no.nav.saf.context.gsak.GsakAcl;
import no.nav.saf.context.gsak.domain.GsakSakerTo;
import no.nav.saf.context.mock.MockData;
import no.nav.saf.context.saf.domain.Journalpost;
import no.nav.saf.context.saf.domain.Sak;
import no.nav.saf.context.saf.domain.Tema;
import no.nav.saf.context.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.context.saf.domain.kode.Temakode;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
class SakstilknyttedeJournalposterCoordinatorImpl implements SakstilknyttedeJournalposterCoordinator {

	private final GsakAcl gsakAcl;

	@Inject
	SakstilknyttedeJournalposterCoordinatorImpl(GsakAcl gsakAcl) {
		this.gsakAcl = gsakAcl;
	}

	@Override
	public Set<Tema> findTemaKnyttetTilAktoerIdAndFilterByTemakoder(final String aktoerId, final List<Temakode> temakoder) {
		return gsakAcl.findTemaByAktoerIdAndFilterTemakode(aktoerId, temakoder);
	}

	@Override
	public List<Sak> findSakerByAktoerIdAndTema(String aktoerId, Temakode tema) {
		return gsakAcl.findSakByAktoerIdAndTemakode(aktoerId, tema);
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
