package no.nav.saf.hentdokument;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.HentDokument;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.exceptions.HentdokumentTilgangskontrollException;
import no.nav.saf.exceptions.JournalpostIkkeFunnetException;
import no.nav.saf.hentdokument.repo.DokumentRepository;
import no.nav.saf.hentdokument.repo.TilgangsmodellHentdokumentRepository;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.pep.DenyReasons;
import no.nav.saf.tilgangskontroll.pep.Pep;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static no.nav.saf.domain.DomainConstants.PEP1G;
import static no.nav.saf.domain.DomainConstants.PEP2;
import static no.nav.saf.domain.DomainConstants.PEP2D;
import static no.nav.saf.domain.DomainConstants.PEP3;
import static no.nav.saf.domain.DomainConstants.PEP4;
import static no.nav.saf.domain.DomainConstants.PEP5;
import static no.nav.saf.domain.DomainConstants.PEP6D;
import static no.nav.saf.domain.DomainConstants.PEP7;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */

@Slf4j
@Component
public class HentDokumentDomainCoordinatorImpl implements HentDokumentDomainCoordinator {

	private final DokumentRepository dokumentRepository;
	private final TilgangsmodellHentdokumentRepository tilgangsmodellHentdokumentRepository;
	private final Pep<TilgangBruker> pep1g;
	private final Pep<TilgangSak> pep2;
	private final Pep<TilgangSak> pep2d;
	private final Pep<TilgangSak> pep3;
	private final Pep<TilgangJournalpost> pep4;
	private final Pep<TilgangDokumentInfo> pep5;
	private final Pep<TilgangDokumentvariant> pep6d;
	private final Pep<List<String>> pep7;
	private final HentDokumentSporbarhetslogger hentDokumentSporbarhetslogger;

	@Inject
	public HentDokumentDomainCoordinatorImpl(DokumentRepository dokumentRepository,
											 TilgangsmodellHentdokumentRepository tilgangsmodellHentdokumentRepository,
											 @Named(PEP1G) Pep<TilgangBruker> pep1g,
											 @Named(PEP2) Pep<TilgangSak> pep2,
											 @Named(PEP2D) Pep<TilgangSak> pep2d,
											 @Named(PEP3) Pep<TilgangSak> pep3,
											 @Named(PEP4) Pep<TilgangJournalpost> pep4,
											 @Named(PEP5) Pep<TilgangDokumentInfo> pep5,
											 @Named(PEP6D) Pep<TilgangDokumentvariant> pep6d,
											 @Named(PEP7) Pep<List<String>> pep7) {
		this.dokumentRepository = dokumentRepository;
		this.tilgangsmodellHentdokumentRepository = tilgangsmodellHentdokumentRepository;
		this.pep1g = pep1g;
		this.pep2 = pep2;
		this.pep2d = pep2d;
		this.pep3 = pep3;
		this.pep4 = pep4;
		this.pep5 = pep5;
		this.pep6d = pep6d;
		this.pep7 = pep7;
		this.hentDokumentSporbarhetslogger = new HentDokumentSporbarhetslogger();
	}

	@Override
	public HentDokument hentDokument(final String journalpostId, final String dokumentInfoId, final String variantFormat, final SafRequestContext safRequestContext) {
		final Arkivsak arkivsak = tilgangsmodellHentdokumentRepository.findArkivsakAndCacheJournalpostDto(journalpostId, dokumentInfoId, variantFormat, safRequestContext);
		final TilgangBruker tilgangBruker = tilgangsmodellHentdokumentRepository.findTilgangBruker(arkivsak, safRequestContext);
		final TilgangSak tilgangSak = tilgangsmodellHentdokumentRepository.findTilgangSak(arkivsak, tilgangBruker, safRequestContext);

		try {
			doTilgangskontroll(journalpostId, dokumentInfoId, variantFormat, tilgangSak, tilgangBruker, safRequestContext);
			hentDokumentSporbarhetslogger.logPermit(journalpostId, dokumentInfoId, variantFormat, tilgangSak, tilgangBruker, safRequestContext);
			return dokumentRepository.findDokument(dokumentInfoId, variantFormat);
		} catch (HentdokumentTilgangskontrollException e) {
			hentDokumentSporbarhetslogger.logDeny(journalpostId, dokumentInfoId, variantFormat, tilgangSak, tilgangBruker, safRequestContext, e);
			throw e;
		}
	}

	private void doTilgangskontroll(String journalpostId, String dokumentInfoId, String variantFormat, TilgangSak tilgangSak, TilgangBruker tilgangBruker, SafRequestContext safRequestContext) {
		XacmlResponse pep1gResponse = pep1g.verifyAccessXacmlResponse(tilgangBruker, safRequestContext);
		if (pep1gResponse.isDeny()) {
			throw new HentdokumentTilgangskontrollException(DenyReasons.PEP1G_DENY_REASON, pep1gResponse);
		}

		XacmlResponse pep2Response = pep2.verifyAccessXacmlResponse(tilgangSak, safRequestContext);
		if (pep2Response.isDeny()) {
			throw new HentdokumentTilgangskontrollException(DenyReasons.PEP2_DENY_REASON, pep2Response);
		}

		XacmlResponse pep3Response = pep3.verifyAccessXacmlResponse(tilgangSak, safRequestContext);
		if (pep3Response.isDeny()) {
			throw new HentdokumentTilgangskontrollException(DenyReasons.PEP3_DENY_REASON, pep3Response);
		}

		final TilgangJournalpost tilgangJournalpost = tilgangsmodellHentdokumentRepository.findTilgangJournalpostFromSafRequestContext(safRequestContext);
		if (tilgangJournalpost == null) {
			throw new JournalpostIkkeFunnetException("Dokumentet tilnyttet journalpostId=" + journalpostId + ", dokumentInfoId=" + dokumentInfoId + ", variant=" + variantFormat + " ikke funnet.");
		}
		if (tilgangJournalpost.getJournalstatus() != Journalstatus.MOTTATT) {
			XacmlResponse pep2dResponse = pep2d.verifyAccessXacmlResponse(tilgangSak, safRequestContext);
			if (pep2dResponse.isDeny()) {
				throw new HentdokumentTilgangskontrollException(DenyReasons.PEP2D_DENY_REASON, pep2dResponse);
			}
		}

		XacmlResponse pep4Response = pep4.verifyAccessXacmlResponse(tilgangJournalpost, safRequestContext);
		if (pep4Response.isDeny()) {
			throw new HentdokumentTilgangskontrollException(DenyReasons.PEP4_DENY_REASON, pep4Response);
		}

		XacmlResponse pep5Response = pep5.verifyAccessXacmlResponse(tilgangJournalpost.getDokumenter().get(0), safRequestContext);
		if (pep5Response.isDeny()) {
			throw new HentdokumentTilgangskontrollException(DenyReasons.PEP5_DENY_REASON, pep5Response);
		}

		XacmlResponse pep6dResponse = pep6d.verifyAccessXacmlResponse(tilgangJournalpost.getDokumenter()
				.get(0).getTilgangDokumentvarianter().get(0), safRequestContext);

		if (pep6dResponse.isDeny()) {
			throw new HentdokumentTilgangskontrollException(DenyReasons.PEP6D_DENY_REASON, pep6dResponse);
		}

		if (tilgangSak != null) {
			List<String> aktoerIds = tilgangsmodellHentdokumentRepository.findRelevanteParterSak(tilgangSak);

			log.info("Hentet {} aktoerId fra fpsak", aktoerIds.size());

			if (!aktoerIds.isEmpty()) {
				XacmlResponse pep7Response = pep7.verifyAccessXacmlResponse(aktoerIds, safRequestContext);
				if (pep7Response.isDeny()) {
					throw new HentdokumentTilgangskontrollException(DenyReasons.PEP7_DENY_REASON, pep7Response);
				}
			}
		}
	}
}
