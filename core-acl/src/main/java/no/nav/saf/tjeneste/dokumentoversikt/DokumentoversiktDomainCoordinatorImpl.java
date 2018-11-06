package no.nav.saf.tjeneste.dokumentoversikt;

import no.nav.saf.domain.TilgangsmodellRepository;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.PepEvaluator;
import no.nav.saf.tjeneste.visningsmodell.DokumentInfo;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import no.nav.saf.tjeneste.visningsmodell.kode.Temakode;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class DokumentoversiktDomainCoordinatorImpl implements DokumentoversiktDomainCoordinator {

	private final TilgangsmodellRepository tilgangsmodellRepository;
	private final DokumentoversiktVisningsmodellRepository visningsmodellRepository;
	private final PepEvaluator<TilgangBruker> pep1;

	@Inject
	public DokumentoversiktDomainCoordinatorImpl(TilgangsmodellRepository tilgangsmodellRepository,
												 DokumentoversiktVisningsmodellRepository visningsmodellRepository,
												 @Named("pep1") PepEvaluator<TilgangBruker> pep1) {
		this.tilgangsmodellRepository = tilgangsmodellRepository;
		this.visningsmodellRepository = visningsmodellRepository;
		this.pep1 = pep1;
	}

	@Override
	public List<Journalpost> findJournalposter(DokumentoversiktArguments dokumentoversiktArguments, SafRequestContext safRequestContext) {
		TilgangBruker tilgangBruker = tilgangsmodellRepository.findTilgangBrukerByAktoerId(dokumentoversiktArguments.getAktoerId());
		boolean pep1Access = this.pep1.hasAccess(tilgangBruker, safRequestContext);
		if(!pep1Access) {
			return new ArrayList<>();
		}
		// TODO Pep2 TilgangSak her
		// TODO Pep3 hvis tema=BID eller FAR
		// TODO Pep4 for TilgangJournalpost her
		return visningsmodellRepository.findJournalposterByAktoerId(dokumentoversiktArguments.getAktoerId(), Temakode.asList())
				.stream()
				// TODO midlertidig. kallet mot joark burde returnere bare det man behøver
				.filter(j -> j.getOpprettet().toLocalDate().isAfter(dokumentoversiktArguments.getFraDato()))
				.filter(j -> dokumentoversiktArguments.getJournalposttyper().contains(j.getJournalposttype()))
				.filter(j -> dokumentoversiktArguments.getJournalstatuser().contains(j.getJournalstatus()))
				.collect(Collectors.toList());
	}

	@Override
	public List<DokumentInfo> findDokumenter(Journalpost journalpost, SafRequestContext safRequestContext) {
		// TODO Pep4 for TilgangDokument her (er dette allerede er avklart i TilgangJournalpost så må context vite om dette)
		return journalpost.getDokumenter();
	}
}
