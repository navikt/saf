package no.nav.saf.hentdokument;

import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.HentDokument;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.exceptions.TilgangskontrollException;
import no.nav.saf.hentdokument.repo.DokumentRepository;
import no.nav.saf.hentdokument.repo.TilgangsmodellHentdokumentRepository;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.Pep;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */

@Component
public class HentDokumentDomainCoordinatorImpl implements HentDokumentDomainCoordinator {

	private final DokumentRepository dokumentRepository;
	private final TilgangsmodellHentdokumentRepository tilgangsmodellHentdokumentRepository;
	private final Pep<TilgangBruker> pep1;
	private final Pep<TilgangSak> pep2;
	private final Pep<TilgangSak> pep2d;
	private final Pep<TilgangSak> pep3;
	private final Pep<TilgangJournalpost> pep4;

	@Inject
	public HentDokumentDomainCoordinatorImpl(DokumentRepository dokumentRepository,
											 TilgangsmodellHentdokumentRepository tilgangsmodellHentdokumentRepository,
											 @Named("pep1") Pep<TilgangBruker> pep1,
											 @Named("pep2") Pep<TilgangSak> pep2,
											 @Named("pep2d") Pep<TilgangSak> pep2d,
											 @Named("pep3") Pep<TilgangSak> pep3,
											 @Named("pep4") Pep<TilgangJournalpost> pep4) {
		this.dokumentRepository = dokumentRepository;
		this.tilgangsmodellHentdokumentRepository = tilgangsmodellHentdokumentRepository;
		this.pep1 = pep1;
		this.pep2 = pep2;
		this.pep2d = pep2d;
		this.pep3 = pep3;
		this.pep4 = pep4;
	}

	@Override
	public HentDokument hentDokument(final String journalpostId, final String dokumentInfoId, final String variantFormat, final SafRequestContext safRequestContext) {
		final Arkivsak arkivsak = tilgangsmodellHentdokumentRepository.findArkivsakAndCacheJournalpostDto(journalpostId, dokumentInfoId, variantFormat, safRequestContext);
		final TilgangBruker tilgangBruker = tilgangsmodellHentdokumentRepository.findTilgangBruker(arkivsak, safRequestContext);

		boolean pep1Access = pep1.hasAccess(tilgangBruker, safRequestContext);
		if (!pep1Access) {
			throw new TilgangskontrollException();
		}

		final TilgangSak tilgangSak = tilgangsmodellHentdokumentRepository.findTilgangSak(arkivsak.getArkivsaksnummer(), arkivsak
				.getArkivsaksystem() == null ? null : arkivsak.getArkivsaksystem().name(), tilgangBruker, safRequestContext);

		boolean pep2Access = pep2.hasAccess(tilgangSak, safRequestContext);
		if (!pep2Access) {
			throw new TilgangskontrollException();
		}

		boolean pep2dAccess = pep2d.hasAccess(tilgangSak, safRequestContext);
		if (!pep2dAccess) {
			throw new TilgangskontrollException();
		}

		boolean pep3Access = pep3.hasAccess(tilgangSak, safRequestContext);
		if (!pep3Access) {
			throw new TilgangskontrollException();
		}

		final TilgangJournalpost tilgangJournalpost = tilgangsmodellHentdokumentRepository.findTilgangJournalpostFromSafRequestContext(safRequestContext, tilgangSak);
		boolean pep4Access = pep4.hasAccess(tilgangJournalpost, safRequestContext);

		if (!pep4Access) {
			throw new TilgangskontrollException();
		}

		return dokumentRepository.findDokument(dokumentInfoId, variantFormat);
	}

}
