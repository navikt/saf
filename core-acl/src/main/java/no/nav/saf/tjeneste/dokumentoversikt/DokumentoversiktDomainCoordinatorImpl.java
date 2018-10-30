package no.nav.saf.tjeneste.dokumentoversikt;

import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.visningsmodell.Bruker;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class DokumentoversiktDomainCoordinatorImpl implements DokumentoversiktDomainCoordinator {

	private final DokumentoversiktVisningsmodellRepository repository;

	@Inject
	public DokumentoversiktDomainCoordinatorImpl(DokumentoversiktVisningsmodellRepository repository) {
		this.repository = repository;
	}

	@Override
	public Bruker findBrukerByAktoerId(String aktoerId, SafRequestContext safRequestContext) {
		// TODO Pep1 her
		return Bruker.builder().aktoerId(aktoerId).build();
	}

	@Override
	public List<Journalpost> findJournalposterByAktoerId(String aktoerId, SafRequestContext safRequestContext) {
		// TODO Pep2, Pep3 og Pep4 her
		return repository.findJournalposterByAktoerId(aktoerId);
	}
}
