package no.nav.saf.tjeneste.hentdokument;

import no.nav.saf.domain.TilgangsmodellRepository;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.PepEvaluator;

import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class HentDokumentDomainCoordinatorImpl implements HentDokumentDomainCoordinator {

	private final TilgangsmodellRepository tilgangsmodellRepository;
	private final PepEvaluator<TilgangBruker> pep1;
	private final PepEvaluator<TilgangSak> pep2;
	private final PepEvaluator<TilgangSak> pep3;
	private final PepEvaluator<TilgangJournalpost> pep4;

	public HentDokumentDomainCoordinatorImpl(TilgangsmodellRepository tilgangsmodellRepository,
											 @Named("pep1") PepEvaluator<TilgangBruker> pep1,
											 @Named("pep2") PepEvaluator<TilgangSak> pep2,
											 @Named("pep3") PepEvaluator<TilgangSak> pep3,
											 @Named("pep4") PepEvaluator<TilgangJournalpost> pep4) {
		this.tilgangsmodellRepository = tilgangsmodellRepository;
		this.pep1 = pep1;
		this.pep2 = pep2;
		this.pep3 = pep3;
		this.pep4 = pep4;
	}

	@Override
	public HentDokumentResponse hentDokument(final HentDokumentArguments hentDokumentArguments, final SafRequestContext safRequestContext) {

		final TilgangBruker tilgangBruker = tilgangsmodellRepository.findTilgangBrukerByAktoerId(hentDokumentArguments.getAktoerId());
		boolean pep1Access = this.pep1.hasAccess(tilgangBruker, safRequestContext);

		if (!pep1Access) {
			return null;
		}

		final List<TilgangSak> tilgangSakList = tilgangsmodellRepository.findTilgangSakListByAktoerId(hentDokumentArguments.getAktoerId());
		List<TilgangSak> filteredTilgangSakList = tilgangSakList.stream()
				.filter(tilgangSak -> pep2.hasAccess(tilgangSak, safRequestContext))
				.filter(tilgangSak -> pep3.hasAccess(tilgangSak, safRequestContext))
				.collect(Collectors.toList());

		final List<TilgangJournalpost> tilgangJournalpostList = tilgangsmodellRepository.findTilgangJournalpostListByArkivsaker(filteredTilgangSakList);

//		final TilgangJournalpost tilgangJournalpost = tilgangJournalpostList.stream()
//				.findAny(tilgangJp -> tilgangJp.getJournalpostId().equals(hentDokumentArguments.getJournalpostId()))
//
//
//		if (!hasSakAccessToJp) {
//			return null;
//		}
//
//		final List<TilgangJournalpost> filteredTilgangJournalpostList = tilgangJournalpostList.stream()
//				.filter(tilgangJournalpost -> pep4.hasAccess(tilgangJournalpost, safRequestContext))
//				.collect(Collectors.toList());

		return HentDokumentResponse.builder().build();
	}
}
