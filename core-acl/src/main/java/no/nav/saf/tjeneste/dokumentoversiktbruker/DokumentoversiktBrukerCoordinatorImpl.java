package no.nav.saf.tjeneste.dokumentoversiktbruker;

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
public class DokumentoversiktBrukerCoordinatorImpl implements DokumentoversiktBrukerCoordinator {

	private final TilgangsmodellRepository tilgangsmodellRepository;
	private final DokumentoversiktBrukerVisningsmodellRepository visningsmodellRepository;
	private final PepEvaluator<TilgangBruker> pep1;
	private final PepEvaluator<TilgangSak> pep2;
	private final PepEvaluator<TilgangSak> pep3;
	private final PepEvaluator<TilgangJournalpost> pep4;

	@Inject
	public DokumentoversiktBrukerCoordinatorImpl(TilgangsmodellRepository tilgangsmodellRepository,
												 DokumentoversiktBrukerVisningsmodellRepository visningsmodellRepository,
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
	public List<Journalpost> findJournalposter(final DokumentoversiktBrukerArguments dokumentoversiktBrukerArguments, final SafRequestContext safRequestContext) {
		final TilgangBruker tilgangBruker = tilgangsmodellRepository.findTilgangBruker(dokumentoversiktBrukerArguments.getAktoerId());
		boolean pep1Access = this.pep1.hasAccess(tilgangBruker, safRequestContext);

		if (!pep1Access) {
			return new ArrayList<>();
		}

		final List<TilgangSak> tilgangSakList = tilgangsmodellRepository.findTilgangSakListByTilgangBruker(tilgangBruker);
		// TODO parallell MMA-1057, MMA-1058
		List<TilgangSak> filteredTilgangSakList = tilgangSakList.stream()
				.filter(tilgangSak -> pep2.hasAccess(tilgangSak, safRequestContext))
				.filter(tilgangSak -> pep3.hasAccess(tilgangSak, safRequestContext))
				.collect(Collectors.toList());

		final List<TilgangJournalpost> tilgangJournalpostList = tilgangsmodellRepository.findTilgangJournalposter(tilgangBruker,
				filteredTilgangSakList,
				dokumentoversiktBrukerArguments.getFraDato(),
				filteredTilgangSakList.stream().map(s -> Temakode.valueOf(s.getTema())).collect(Collectors.toSet()),
				dokumentoversiktBrukerArguments.getJournalposttyper(),
				dokumentoversiktBrukerArguments.getJournalstatuser()
		);

		// TODO parallell MMA-1091
		final List<TilgangJournalpost> filteredTilgangJournalpostList = tilgangJournalpostList.stream()
				.filter(tilgangJournalpost -> pep4.hasAccess(tilgangJournalpost, safRequestContext))
				.collect(Collectors.toList());

		return visningsmodellRepository.findJournalposter(tilgangBruker.getAktoerId(), tilgangBruker.getFoedselsnr(),
				filteredTilgangJournalpostList.stream().map(TilgangJournalpost::getJournalpostId).collect(Collectors.toList()));
	}

	@Override
	public List<DokumentInfo> findDokumenter(Journalpost journalpost, SafRequestContext safRequestContext) {
		// TODO MMA-1092 Pep4 for TilgangDokument her (er dette allerede er avklart i TilgangJournalpost så må context vite om dette)
		return journalpost.getDokumenter();
	}
}
