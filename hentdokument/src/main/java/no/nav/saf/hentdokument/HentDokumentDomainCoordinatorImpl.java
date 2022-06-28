package no.nav.saf.hentdokument;

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
import no.nav.saf.tilgangskontroll.pep.AbacAnswer;
import no.nav.saf.tilgangskontroll.pep.Pep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static no.nav.saf.tilgangskontroll.pep.DenyReasons.PEP1G_DENY_REASON;
import static no.nav.saf.tilgangskontroll.pep.DenyReasons.PEP2D_DENY_REASON;
import static no.nav.saf.tilgangskontroll.pep.DenyReasons.PEP2_DENY_REASON;
import static no.nav.saf.tilgangskontroll.pep.DenyReasons.PEP3_DENY_REASON;
import static no.nav.saf.tilgangskontroll.pep.DenyReasons.PEP4_DENY_REASON;
import static no.nav.saf.tilgangskontroll.pep.DenyReasons.PEP5_DENY_REASON;
import static no.nav.saf.tilgangskontroll.pep.DenyReasons.PEP6D_DENY_REASON;
import static no.nav.saf.tilgangskontroll.pep.DenyReasons.PEP7D_DENY_REASON;

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
	private final Pep<TilgangSak> pep7d;
	private final HentDokumentSporbarhetslogger hentDokumentSporbarhetslogger;

	@Autowired
	public HentDokumentDomainCoordinatorImpl(DokumentRepository dokumentRepository,
											 TilgangsmodellHentdokumentRepository tilgangsmodellHentdokumentRepository,
											 @Autowired Pep<TilgangBruker> pep1g,
											 @Autowired Pep<TilgangSak> pep2,
											 @Autowired Pep<TilgangSak> pep2d,
											 @Autowired Pep<TilgangSak> pep3,
											 @Autowired Pep<TilgangJournalpost> pep4,
											 @Autowired Pep<TilgangDokumentInfo> pep5,
											 @Autowired Pep<TilgangDokumentvariant> pep6d,
											 @Autowired Pep<TilgangSak> pep7d) {
		this.dokumentRepository = dokumentRepository;
		this.tilgangsmodellHentdokumentRepository = tilgangsmodellHentdokumentRepository;
		this.pep1g = pep1g;
		this.pep2 = pep2;
		this.pep2d = pep2d;
		this.pep3 = pep3;
		this.pep4 = pep4;
		this.pep5 = pep5;
		this.pep6d = pep6d;
		this.pep7d = pep7d;
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
		AbacAnswer pep1gResponse = pep1g.hasAccessWithAnswer(tilgangBruker, safRequestContext);
		if (pep1gResponse.isDeny()) {
			throw new HentdokumentTilgangskontrollException(PEP1G_DENY_REASON, pep1gResponse.getDenyReasonSporing());
		}

		AbacAnswer pep2Response = pep2.hasAccessWithAnswer(tilgangSak, safRequestContext);
		if (pep2Response.isDeny()) {
			throw new HentdokumentTilgangskontrollException(PEP2_DENY_REASON, pep2Response.getDenyReasonSporing());
		}

		AbacAnswer pep3Response = pep3.hasAccessWithAnswer(tilgangSak, safRequestContext);
		if (pep3Response.isDeny()) {
			throw new HentdokumentTilgangskontrollException(PEP3_DENY_REASON, pep3Response.getDenyReasonSporing());
		}

		final TilgangJournalpost tilgangJournalpost = tilgangsmodellHentdokumentRepository.findTilgangJournalpostFromSafRequestContext(safRequestContext);
		if (tilgangJournalpost == null) {
			throw new JournalpostIkkeFunnetException("Dokumentet tilnyttet journalpostId=" + journalpostId + ", dokumentInfoId=" + dokumentInfoId + ", variant=" + variantFormat + " ikke funnet.");
		}
		if (tilgangJournalpost.getJournalstatus() != Journalstatus.MOTTATT) {
			AbacAnswer pep2dResponse = pep2d.hasAccessWithAnswer(tilgangSak, safRequestContext);
			if (pep2dResponse.isDeny()) {
				throw new HentdokumentTilgangskontrollException(PEP2D_DENY_REASON, pep2dResponse.getDenyReasonSporing());
			}
		}

		AbacAnswer pep4Response = pep4.hasAccessWithAnswer(tilgangJournalpost, safRequestContext);
		if (pep4Response.isDeny()) {
			throw new HentdokumentTilgangskontrollException(PEP4_DENY_REASON, pep4Response.getDenyReasonSporing());
		}

		final TilgangDokumentInfo tilgangDokumentInfo = tilgangJournalpost.getDokumenter().get(0);
		AbacAnswer pep5Response = pep5.hasAccessWithAnswer(tilgangDokumentInfo, safRequestContext);
		if (pep5Response.isDeny()) {
			throw new HentdokumentTilgangskontrollException(PEP5_DENY_REASON, pep5Response.getDenyReasonSporing());
		}

		AbacAnswer pep6dResponse = pep6d.hasAccessWithAnswer(tilgangDokumentInfo.getTilgangDokumentvarianter().get(0), safRequestContext);
		if (pep6dResponse.isDeny()) {
			throw new HentdokumentTilgangskontrollException(PEP6D_DENY_REASON, pep6dResponse.getDenyReasonSporing());
		}

		AbacAnswer pep7dResponse = pep7d.hasAccessWithAnswer(tilgangSak, safRequestContext);
		if (pep7dResponse.isDeny()) {
			throw new HentdokumentTilgangskontrollException(PEP7D_DENY_REASON, pep7dResponse.getDenyReasonSporing());
		}
	}
}
