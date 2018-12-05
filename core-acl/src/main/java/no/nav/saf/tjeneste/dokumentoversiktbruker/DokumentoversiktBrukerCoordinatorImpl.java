package no.nav.saf.tjeneste.dokumentoversiktbruker;

import no.nav.saf.domain.TilgangsmodellRepository;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.Pep;
import no.nav.saf.tjeneste.visningsmodell.DokumentInfo;
import no.nav.saf.tjeneste.visningsmodell.Journalpost;
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
	private final Pep<TilgangBruker> pep1;
	private final Pep<TilgangSak> pep2;
	private final Pep<TilgangSak> pep3;
	private final Pep<TilgangJournalpost> pep4;

	@Inject
	public DokumentoversiktBrukerCoordinatorImpl(TilgangsmodellRepository tilgangsmodellRepository,
												 DokumentoversiktBrukerVisningsmodellRepository visningsmodellRepository,
												 @Named("pep1") Pep<TilgangBruker> pep1,
												 @Named("pep2") Pep<TilgangSak> pep2,
												 @Named("pep3") Pep<TilgangSak> pep3,
												 @Named("pep4") Pep<TilgangJournalpost> pep4) {
		this.tilgangsmodellRepository = tilgangsmodellRepository;
		this.visningsmodellRepository = visningsmodellRepository;
		this.pep1 = pep1;
		this.pep2 = pep2;
		this.pep3 = pep3;
		this.pep4 = pep4;
	}

	@Override
	public List<Journalpost> findJournalposter(final DokumentoversiktBrukerArguments dokumentoversiktBrukerArguments, final SafRequestContext safRequestContext) {
		final TilgangBruker tilgangBruker = tilgangsmodellRepository.findTilgangBruker(dokumentoversiktBrukerArguments.getBrukerIdInput());
		safRequestContext.getRequestCache().putObject("tilgangBruker", tilgangBruker);
		boolean pep1Access = this.pep1.hasAccess(tilgangBruker, safRequestContext);

		if (!pep1Access) {
			return new ArrayList<>();
		}

		final List<TilgangSak> tilgangSakList = tilgangsmodellRepository.findTilgangSaker(tilgangBruker, dokumentoversiktBrukerArguments.getTema(), safRequestContext);
		List<TilgangSak> filteredTilgangSakList = tilgangSakList.stream()
				.filter(ts -> pep2.hasAccess(ts, safRequestContext))
				.filter(ts -> pep3.hasAccess(ts, safRequestContext))
				.collect(Collectors.toList());

		final List<TilgangJournalpost> tilgangJournalpostList = tilgangsmodellRepository.findTilgangJournalposter(
				tilgangBruker,
				filteredTilgangSakList,
				dokumentoversiktBrukerArguments.getFraDato(),
				dokumentoversiktBrukerArguments.getTema(),
				dokumentoversiktBrukerArguments.getJournalposttyper(),
				dokumentoversiktBrukerArguments.getJournalstatuser(),
				safRequestContext
		);

		final List<TilgangJournalpost> filteredTilgangJournalpostList = tilgangJournalpostList.stream()
				.filter(tj -> pep4.hasAccess(tj, safRequestContext))
				.collect(Collectors.toList());

		return visningsmodellRepository.findJournalposter(filteredTilgangJournalpostList.stream().map(TilgangJournalpost::getJournalpostId).collect(Collectors.toList()), safRequestContext);
	}

	@Override
	public List<DokumentInfo> findDokumenter(final Journalpost journalpost, final SafRequestContext safRequestContext) {
		// TODO MMA-1092 Pep4 for TilgangDokument her (er dette allerede er avklart i TilgangJournalpost så må context vite om dette)
		return journalpost.getDokumenter();
	}
}
