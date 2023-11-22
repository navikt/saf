package no.nav.saf.query.journalpost;

import graphql.schema.DataFetchingEnvironment;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.exceptions.JournalpostIkkeFunnetException;
import no.nav.saf.graphql.GraphQLException;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.AbacAnswer;
import no.nav.saf.tilgangskontroll.pep.Pep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostMapper.mapJournalpost;
import static no.nav.saf.domain.DomainConstants.TILGANG_BRUKER;
import static no.nav.saf.domain.kode.Journalstatus.MOTTATT;
import static no.nav.saf.graphql.ErrorCode.FORBIDDEN;
import static no.nav.saf.graphql.ErrorCode.NOT_FOUND;
import static no.nav.saf.tilgangskontroll.pep.DenyReasonFactory.createPep1gDenyReason;
import static no.nav.saf.tilgangskontroll.pep.DenyReasonFactory.createPep2DenyReason;
import static no.nav.saf.tilgangskontroll.pep.DenyReasonFactory.createPep3DenyReason;
import static no.nav.saf.tilgangskontroll.pep.DenyReasonFactory.createPep4DenyReason;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Component
class JournalpostQuery {

	private final JournalpostService journalpostService;
	private final Pep<TilgangBruker> pep1g;
	private final Pep<TilgangSak> pep2;
	private final Pep<TilgangSak> pep2d;
	private final Pep<TilgangSak> pep3;
	private final Pep<TilgangJournalpost> pep4;
	private final Pep<TilgangDokumentInfo> pep5;
	private final Pep<TilgangDokumentvariant> pep6d;
	private final Pep<TilgangSak> pep7d;

	public JournalpostQuery(
			JournalpostService journalpostService,
			@Autowired Pep<TilgangBruker> pep1g,
			@Autowired Pep<TilgangSak> pep2,
			@Autowired Pep<TilgangSak> pep2d,
			@Autowired Pep<TilgangSak> pep3,
			@Autowired Pep<TilgangJournalpost> pep4,
			@Autowired Pep<TilgangDokumentInfo> pep5,
			@Autowired Pep<TilgangDokumentvariant> pep6d,
			@Autowired Pep<TilgangSak> pep7d) {
		this.journalpostService = journalpostService;
		this.pep1g = pep1g;
		this.pep2 = pep2;
		this.pep2d = pep2d;
		this.pep3 = pep3;
		this.pep4 = pep4;
		this.pep5 = pep5;
		this.pep6d = pep6d;
		this.pep7d = pep7d;
	}

	public Journalpost hentJournalpost(final String journalpostId, String eksternReferanseId,
									   final SafRequestContext safRequestContext,
									   final DataFetchingEnvironment environment) {
		try {
			JournalpostHolder journalpostHolder = journalpostService.hentJournalpost(journalpostId, eksternReferanseId, safRequestContext);

			TilgangSak tilgangSak = journalpostHolder.journalpostTilgang().tilgangSak();
			TilgangBruker tilgangBruker = journalpostHolder.journalpostTilgang().tilgangBruker();
			TilgangJournalpost tilgangJournalpost = journalpostHolder.journalpostTilgang().tilgangJournalpost();

			if (tilgangBruker != null) {
				safRequestContext.getRequestCache().putObject(TILGANG_BRUKER, tilgangBruker);
			}

			AbacAnswer pep1Access = pep1g.hasAccessWithAnswer(tilgangBruker, safRequestContext);
			if (pep1Access.isDeny()) {
				throw GraphQLException.of(FORBIDDEN, environment, createPep1gDenyReason(safRequestContext, pep1Access));
			}

			boolean pep2Access = pep2.hasAccess(tilgangSak, safRequestContext);
			if (!pep2Access) {
				throw GraphQLException.of(FORBIDDEN, environment, createPep2DenyReason(safRequestContext));
			}

			if (tilgangJournalpost.getJournalstatus() != MOTTATT) {
				pep2d.hasAccess(tilgangSak, safRequestContext);
				pep7d.hasAccess(tilgangSak, safRequestContext);
			}

			boolean pep3Access = pep3.hasAccess(tilgangSak, safRequestContext);
			if (!pep3Access) {
				throw GraphQLException.of(FORBIDDEN, environment, createPep3DenyReason(safRequestContext));
			}

			boolean pep4Access = pep4.hasAccess(tilgangJournalpost, safRequestContext);
			if (!pep4Access) {
				throw GraphQLException.of(FORBIDDEN, environment, createPep4DenyReason(safRequestContext));
			}

			tilgangJournalpost.getDokumenter().forEach(tilgangDokumentInfo -> {
				pep5.hasAccess(tilgangDokumentInfo, safRequestContext);
				tilgangDokumentInfo.getTilgangDokumentvarianter().forEach(tilgangDokumentvariant ->
						pep6d.hasAccess(tilgangDokumentvariant, safRequestContext));
			});

			return mapJournalpost(journalpostHolder.arkivJournalpost(), safRequestContext.getRequestCache());
		} catch (JournalpostIkkeFunnetException e) {
			throw GraphQLException.of(NOT_FOUND, environment,
					"Fant ikke journalpost i fagarkivet. " + errLog(journalpostId, eksternReferanseId));
		}
	}

	private String errLog(String journalpostId, String eksternReferanseId) {
		return (isBlank(journalpostId) ? "eksternReferanseId=" : "journalpostId=") + (isBlank(journalpostId) ? eksternReferanseId : journalpostId);
	}
}
