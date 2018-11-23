package no.nav.saf.tjeneste.dokumentoversiktbruker;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
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
		final TilgangBruker tilgangBruker = tilgangsmodellRepository.findTilgangBruker(dokumentoversiktBrukerArguments.getBrukeridentifikator());
		boolean pep1Access = this.pep1.hasAccess(tilgangBruker, safRequestContext);

		if (!pep1Access) {
			return new ArrayList<>();
		}

		final List<TilgangSak> tilgangSakList = tilgangsmodellRepository.findTilgangSakListByTilgangBruker(tilgangBruker);
		List<TilgangSak> filteredTilgangSakList = Flowable.fromIterable(tilgangSakList)
				.flatMap(tilgangSak ->
						Flowable.just(tilgangSak)
								.observeOn(Schedulers.io())
								.filter(ts -> pep2.hasAccess(ts, safRequestContext))
								.filter(ts -> pep3.hasAccess(ts, safRequestContext))
				).toList()
				.blockingGet();

		final List<TilgangJournalpost> tilgangJournalpostList = tilgangsmodellRepository.findTilgangJournalposter(tilgangBruker,
				filteredTilgangSakList,
				dokumentoversiktBrukerArguments.getFraDato(),
				filteredTilgangSakList.stream().map(s -> Temakode.valueOf(s.getTema())).collect(Collectors.toSet()),
				dokumentoversiktBrukerArguments.getJournalposttyper(),
				dokumentoversiktBrukerArguments.getJournalstatuser()
		);

		final List<TilgangJournalpost> filteredTilgangJournalpostList = Flowable.fromIterable(tilgangJournalpostList)
				.flatMap(tilgangJournalpost ->
						Flowable.just(tilgangJournalpost)
								.observeOn(Schedulers.io())
								.filter(tj -> pep4.hasAccess(tj, safRequestContext))
				).toList()
				.blockingGet();

		return visningsmodellRepository.findJournalposter(tilgangBruker.getAktoerId(), tilgangBruker.getFoedselsnr(),
				filteredTilgangJournalpostList.stream().map(TilgangJournalpost::getJournalpostId).collect(Collectors.toList()));
	}

	@Override
	public List<DokumentInfo> findDokumenter(Journalpost journalpost, SafRequestContext safRequestContext) {
		// TODO MMA-1092 Pep4 for TilgangDokument her (er dette allerede er avklart i TilgangJournalpost så må context vite om dette)
		return journalpost.getDokumenter();
	}
}
