package no.nav.saf.query.journalpost;

import graphql.schema.DataFetchingEnvironment;
import no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoMapper;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.exceptions.JournalpostIkkeFunnetException;
import no.nav.saf.graphql.GraphQLException;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.Pep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static no.nav.saf.domain.DomainConstants.RJOARK902_JOURNALPOST_DTO;
import static no.nav.saf.domain.DomainConstants.TILGANG_BRUKER;
import static no.nav.saf.domain.kode.Journalstatus.MOTTATT;
import static no.nav.saf.graphql.ErrorCode.FORBIDDEN;
import static no.nav.saf.graphql.ErrorCode.NOT_FOUND;
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
	private final Pep<TilgangBruker> pep1g;
	private final Pep<TilgangSak> pep2;
	private final Pep<TilgangSak> pep2d;
	private final Pep<TilgangSak> pep3;
	private final Pep<TilgangJournalpost> pep4;
	private final Pep<TilgangDokumentInfo> pep5;
	private final Pep<TilgangDokumentvariant> pep6d;
	private final Pep<TilgangSak> pep7d;

	public JournalpostQuery(
			JournalpostTilgangRepository journalpostTilgangRepository,
			JournalpostDtoMapper journalpostDtoMapper,
			@Autowired Pep<TilgangBruker> pep1g,
			@Autowired Pep<TilgangSak> pep2,
			@Autowired Pep<TilgangSak> pep2d,
			@Autowired Pep<TilgangSak> pep3,
			@Autowired Pep<TilgangJournalpost> pep4,
			@Autowired Pep<TilgangDokumentInfo> pep5,
			@Autowired Pep<TilgangDokumentvariant> pep6d,
			@Autowired Pep<TilgangSak> pep7d) {
		this.journalpostTilgangRepository = journalpostTilgangRepository;
		this.journalpostDtoMapper = journalpostDtoMapper;
		this.pep1g = pep1g;
		this.pep2 = pep2;
		this.pep2d = pep2d;
		this.pep3 = pep3;
		this.pep4 = pep4;
		this.pep5 = pep5;
		this.pep6d = pep6d;
		this.pep7d = pep7d;
	}

	public Journalpost hentJournalpost(final String journalpostId,
									   final SafRequestContext safRequestContext,
									   final DataFetchingEnvironment environment) {
		try {
			final Arkivsak arkivsak = journalpostTilgangRepository.findArkivsakAndCacheJournalpostDto(journalpostId, safRequestContext);
			final TilgangBruker tilgangBruker = journalpostTilgangRepository.findTilgangBruker(arkivsak, safRequestContext);

			if (tilgangBruker != null) {
				safRequestContext.getRequestCache().putObject(TILGANG_BRUKER, tilgangBruker);
			}

			boolean pep1Access = pep1g.hasAccess(tilgangBruker, safRequestContext);
			if (!pep1Access) {
				throw GraphQLException.of(FORBIDDEN, environment, PEP1G_DENY_REASON);
			}

			final TilgangSak tilgangSak = journalpostTilgangRepository.findTilgangSak(arkivsak, tilgangBruker, safRequestContext);

			boolean pep2Access = pep2.hasAccess(tilgangSak, safRequestContext);
			if (!pep2Access) {
				throw GraphQLException.of(FORBIDDEN, environment, PEP2_DENY_REASON);
			}

			final TilgangJournalpost tilgangJournalpost = journalpostTilgangRepository.findTilgangJournalpostFromSafRequestContext(safRequestContext, tilgangSak);
			if (tilgangJournalpost.getJournalstatus() != MOTTATT) {
				pep2d.hasAccess(tilgangSak, safRequestContext);
				pep7d.hasAccess(tilgangSak, safRequestContext);
			}

			boolean pep3Access = pep3.hasAccess(tilgangSak, safRequestContext);
			if (!pep3Access) {
				throw GraphQLException.of(FORBIDDEN, environment, PEP3_DENY_REASON);
			}

			boolean pep4Access = pep4.hasAccess(tilgangJournalpost, safRequestContext);
			if (!pep4Access) {
				throw GraphQLException.of(FORBIDDEN, environment, PEP4_DENY_REASON);
			}

			tilgangJournalpost.getDokumenter().forEach(tilgangDokumentInfo -> {
				pep5.hasAccess(tilgangDokumentInfo, safRequestContext);
				tilgangDokumentInfo.getTilgangDokumentvarianter().forEach(tilgangDokumentvariant ->
						pep6d.hasAccess(tilgangDokumentvariant, safRequestContext));
			});

			return hentVisningsmodell(safRequestContext);
		} catch (JournalpostIkkeFunnetException e) {
			throw GraphQLException.of(NOT_FOUND, environment,
					"Fant ikke journalpost i fagarkivet. journalpostId=" + journalpostId);
		}
	}

	private Journalpost hentVisningsmodell(SafRequestContext safRequestContext) {
		final JournalpostDto journalpostDto = safRequestContext.getRequestCache().getObject(RJOARK902_JOURNALPOST_DTO);
		return journalpostDtoMapper.mapJournalpostDto(journalpostDto, safRequestContext.getRequestCache());
	}
}
