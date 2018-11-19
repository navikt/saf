package no.nav.saf.tjeneste.dokumentoversikt;

import no.nav.saf.domain.TilgangsmodellRepository;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
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
	private final PepEvaluator<TilgangSak> pep2;
	private final PepEvaluator<TilgangSak> pep3;
	private final PepEvaluator<TilgangJournalpost> pep4;

	@Inject
	public DokumentoversiktDomainCoordinatorImpl(TilgangsmodellRepository tilgangsmodellRepository,
												 DokumentoversiktVisningsmodellRepository visningsmodellRepository,
												 @Named("pep1") PepEvaluator<TilgangBruker> pep1,
												 @Named("pep2") PepEvaluator<TilgangSak> pep2,
												 @Named("pep3") PepEvaluator<TilgangSak> pep3,
												 @Named("pep4") PepEvaluator<TilgangJournalpost> pep4) {
		this.tilgangsmodellRepository = tilgangsmodellRepository;
		this.visningsmodellRepository = visningsmodellRepository;
		this.pep1 = pep1;
		this.pep2 = pep2;
		this.pep3 = pep3;
		this.pep4 = pep4;
	}

	@Override
	public List<Journalpost> findJournalposter(final DokumentoversiktArguments dokumentoversiktArguments, final SafRequestContext safRequestContext) {
		final TilgangBruker tilgangBruker = tilgangsmodellRepository.findTilgangBrukerByAktoerId(dokumentoversiktArguments.getAktoerId());
		boolean pep1Access = this.pep1.hasAccess(tilgangBruker, safRequestContext);

		if (!pep1Access) {
			return new ArrayList<>();
		}

		final List<TilgangSak> tilgangSakList = tilgangsmodellRepository.findTilgangSakListByTilgangBruker(tilgangBruker);
		List<TilgangSak> filteredTilgangSakList = tilgangSakList.stream()
				.filter(tilgangSak -> pep2.hasAccess(tilgangSak, safRequestContext))
				.filter(tilgangSak -> pep3.hasAccess(tilgangSak, safRequestContext))
				.collect(Collectors.toList());
		final List<TilgangJournalpost> tilgangJournalpostList = tilgangsmodellRepository.findTilgangJournalposter(tilgangBruker,
				filteredTilgangSakList,
				dokumentoversiktArguments.getFraDato(),
				filteredTilgangSakList.stream().map(s -> Temakode.valueOf(s.getTema())).collect(Collectors.toSet()),
				dokumentoversiktArguments.getJournalposttyper(),
				dokumentoversiktArguments.getJournalstatuser()
		);

		final List<TilgangJournalpost> filteredTilgangJournalpostList = tilgangJournalpostList.stream()
				.filter(tilgangJournalpost -> pep4.hasAccess(tilgangJournalpost, safRequestContext))
				.collect(Collectors.toList());
		return visningsmodellRepository.findJournalposter(tilgangBruker.getAktoerId(),
				filteredTilgangJournalpostList.stream().map(TilgangJournalpost::getJournalpostId).collect(Collectors.toList()));
	}

	@Override
	public List<DokumentInfo> findDokumenter(Journalpost journalpost, SafRequestContext safRequestContext) {
		// TODO Pep4 for TilgangDokument her (er dette allerede er avklart i TilgangJournalpost så må context vite om dette)
		return journalpost.getDokumenter();
	}
}
