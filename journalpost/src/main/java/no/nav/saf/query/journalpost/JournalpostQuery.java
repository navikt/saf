package no.nav.saf.query.journalpost;

import no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoMapper;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.exceptions.JournalpostTilgangskontrollException;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.Pep;
import org.springframework.stereotype.Component;

import javax.inject.Named;

import static no.nav.saf.domain.DomainConstants.PEP1G;
import static no.nav.saf.domain.DomainConstants.PEP2;
import static no.nav.saf.domain.DomainConstants.PEP2D;
import static no.nav.saf.domain.DomainConstants.PEP3;
import static no.nav.saf.domain.DomainConstants.PEP4;
import static no.nav.saf.domain.DomainConstants.PEP5;
import static no.nav.saf.domain.DomainConstants.PEP6D;
import static no.nav.saf.domain.DomainConstants.RJOARK902_JOURNALPOST_DTO;
import static no.nav.saf.domain.DomainConstants.TILGANG_BRUKER;
import static no.nav.saf.domain.kode.Journalstatus.MOTTATT;
import static no.nav.saf.tilgangskontroll.pep.DenyReasons.PEP1G_DENY_REASON;
import static no.nav.saf.tilgangskontroll.pep.DenyReasons.PEP2_DENY_REASON;
import static no.nav.saf.tilgangskontroll.pep.DenyReasons.PEP3_DENY_REASON;
import static no.nav.saf.tilgangskontroll.pep.DenyReasons.PEP4_DENY_REASON;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
class JournalpostQuery {

	private final JournalpostTilgangRepository journalpostTilgangRepository;
	private final JournalpostDtoMapper journalpostDtoMapper;
	private final Pep<TilgangBruker> pep1;
	private final Pep<TilgangSak> pep2;
	private final Pep<TilgangSak> pep2d;
	private final Pep<TilgangSak> pep3;
	private final Pep<TilgangJournalpost> pep4;
	private final Pep<TilgangDokumentInfo> pep5;
	private final Pep<TilgangDokumentvariant> pep6d;

	public JournalpostQuery(JournalpostTilgangRepository journalpostTilgangRepository,
							JournalpostDtoMapper journalpostDtoMapper,
							@Named(PEP1G) Pep<TilgangBruker> pep1,
							@Named(PEP2) Pep<TilgangSak> pep2,
							@Named(PEP2D) Pep<TilgangSak> pep2d,
							@Named(PEP3) Pep<TilgangSak> pep3,
							@Named(PEP4) Pep<TilgangJournalpost> pep4,
							@Named(PEP5) Pep<TilgangDokumentInfo> pep5,
							@Named(PEP6D) Pep<TilgangDokumentvariant> pep6d) {
		this.journalpostTilgangRepository = journalpostTilgangRepository;
		this.journalpostDtoMapper = journalpostDtoMapper;
		this.pep1 = pep1;
		this.pep2 = pep2;
		this.pep2d = pep2d;
		this.pep3 = pep3;
		this.pep4 = pep4;
		this.pep5 = pep5;
		this.pep6d = pep6d;
	}

	public Journalpost hentJournalpost(String journalpostId, SafRequestContext safRequestContext) {
		final Arkivsak arkivsak = journalpostTilgangRepository.findArkivsakAndCacheJournalpostDto(journalpostId, safRequestContext);
		final TilgangBruker tilgangBruker = journalpostTilgangRepository.findTilgangBruker(arkivsak, safRequestContext);

		if (tilgangBruker != null) {
			safRequestContext.getRequestCache().putObject(TILGANG_BRUKER, tilgangBruker);
		}

		boolean pep1Access = pep1.hasAccess(tilgangBruker, safRequestContext);
		if (!pep1Access) {
			throw new JournalpostTilgangskontrollException(PEP1G_DENY_REASON);
		}

		final TilgangSak tilgangSak = journalpostTilgangRepository.findTilgangSak(arkivsak, tilgangBruker, safRequestContext);

		boolean pep2Access = pep2.hasAccess(tilgangSak, safRequestContext);
		if (!pep2Access) {
			throw new JournalpostTilgangskontrollException(PEP2_DENY_REASON);
		}

		final TilgangJournalpost tilgangJournalpost = journalpostTilgangRepository.findTilgangJournalpostFromSafRequestContext(safRequestContext, tilgangSak);
		if (tilgangJournalpost.getJournalstatus() != MOTTATT) {
			pep2d.hasAccess(tilgangSak, safRequestContext);
		}

		boolean pep3Access = pep3.hasAccess(tilgangSak, safRequestContext);
		if (!pep3Access) {
			throw new JournalpostTilgangskontrollException(PEP3_DENY_REASON);
		}

		boolean pep4Access = pep4.hasAccess(tilgangJournalpost, safRequestContext);
		if (!pep4Access) {
			throw new JournalpostTilgangskontrollException(PEP4_DENY_REASON);
		}

		tilgangJournalpost.getDokumenter().forEach(tilgangDokumentInfo -> {
			pep5.hasAccess(tilgangDokumentInfo, safRequestContext);
			tilgangDokumentInfo.getTilgangDokumentvarianter().forEach(tilgangDokumentvariant ->
					pep6d.hasAccess(tilgangDokumentvariant, safRequestContext));
		});

		return hentVisningsmodell(safRequestContext);
	}

	private Journalpost hentVisningsmodell(SafRequestContext safRequestContext) {
		final JournalpostDto journalpostDto = safRequestContext.getRequestCache().getObject(RJOARK902_JOURNALPOST_DTO);
		return journalpostDtoMapper.mapJournalpostDto(journalpostDto, safRequestContext.getRequestCache());
	}
}
