package no.nav.saf.tjeneste.hentdokument;

import no.nav.saf.domain.DokumentRepository;
import no.nav.saf.domain.TilgangsmodellRepository;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.PepEvaluator;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */

@Component
public class HentDokumentDomainCoordinatorImpl implements HentDokumentDomainCoordinator {

	private final DokumentRepository dokumentRepository;
	private final TilgangsmodellRepository tilgangsmodellRepository;
	private final PepEvaluator<TilgangBruker> pep1;
	private final PepEvaluator<TilgangSak> pep2;
	private final PepEvaluator<TilgangSak> pep3;
	private final PepEvaluator<TilgangJournalpost> pep4;

	@Inject
	public HentDokumentDomainCoordinatorImpl(DokumentRepository dokumentRepository,
											 TilgangsmodellRepository tilgangsmodellRepository,
											 @Named("pep1") PepEvaluator<TilgangBruker> pep1,
											 @Named("pep2") PepEvaluator<TilgangSak> pep2,
											 @Named("pep3") PepEvaluator<TilgangSak> pep3,
											 @Named("pep4") PepEvaluator<TilgangJournalpost> pep4) {
		this.dokumentRepository = dokumentRepository;
		this.tilgangsmodellRepository = tilgangsmodellRepository;
		this.pep1 = pep1;
		this.pep2 = pep2;
		this.pep3 = pep3;
		this.pep4 = pep4;
	}

	@Override
	public HentDokument hentDokument(final String journalpostId, final String dokumentId, final String variantFormat, final SafRequestContext safRequestContext) {
		final TilgangSak tilgangSak = tilgangsmodellRepository.findTilgangSak(journalpostId, dokumentId, variantFormat);
		final TilgangBruker tilgangBruker = tilgangsmodellRepository.findTilgangBrukerBySakId(tilgangSak.getArkivsaksnummer());

		boolean pep1Access = pep1.hasAccess(tilgangBruker, safRequestContext);
		if (!pep1Access) {
			return new HentDokument();
		}

		boolean pep2Access = pep2.hasAccess(tilgangSak, safRequestContext);
		if (!pep2Access) {
			return new HentDokument();
		}

		boolean pep3Access = pep3.hasAccess(tilgangSak, safRequestContext);
		if (!pep3Access) {
			return new HentDokument();
		}

		final TilgangJournalpost tilgangJournalpost = tilgangsmodellRepository.findTilgangJournalpost(journalpostId, dokumentId, variantFormat);
		boolean pep4Access = pep4.hasAccess(tilgangJournalpost, safRequestContext);

		if (!pep4Access) {
			return new HentDokument();
		}

		return dokumentRepository.findDokument(dokumentId, variantFormat);
	}
}
