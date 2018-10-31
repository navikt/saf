package no.nav.saf.tjeneste.dokumentoversikt;

import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.visningsmodell.DokumentInfo;
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
	public List<Journalpost> findJournalposter(String aktoerId, SafRequestContext safRequestContext) {
		// TODO Pep1 på TilgangBruker aktoerId her
		// TODO Pep2 TilgangSak her
		// TODO Pep3 hvis tema=BID eller FAR
		// TODO Pep4 for TilgangJournalpost her
		return repository.findJournalposterByAktoerId(safRequestContext.getAktoerId());
	}

	@Override
	public List<DokumentInfo> findDokumenter(Journalpost journalpost, SafRequestContext safRequestContext) {
		// TODO Pep4 for TilgangDokument her (er dette allerede er avklart i TilgangJournalpost så må context vite om dette)
		return journalpost.getDokumenter();
	}
}
