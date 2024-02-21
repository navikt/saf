package no.nav.saf.query.journalpost;

import graphql.schema.DataFetchingEnvironment;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.domain.visningsmodell.Journalpost;
import no.nav.saf.exceptions.JournalpostIkkeFunnetException;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.TilgangskontrollException;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.AbacAnswer;
import no.nav.saf.tilgangskontroll.pep.Pep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostMapper.mapJournalpost;
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

	public static final String SELECTION_JOURNALPOST_DOKUMENTER = "Journalpost.dokumenter";
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
			JournalpostHolder journalpostHolder = journalpostService.hentJournalpost(journalpostId, eksternReferanseId, safRequestContext, environment.getSelectionSet());

			TilgangSak tilgangSak = journalpostHolder.journalpostTilgang().tilgangSak();
			TilgangBruker tilgangBruker = journalpostHolder.journalpostTilgang().tilgangBruker();
			TilgangJournalpost tilgangJournalpost = journalpostHolder.journalpostTilgang().tilgangJournalpost();

			if (tilgangBruker != null) {
				safRequestContext.getRequestCache().putTilgangBruker(tilgangBruker);
			}

			AbacAnswer pep1Access = pep1g.hasAccessWithAnswer(tilgangBruker, safRequestContext);
			if (pep1Access.isDeny()) {
				throw new TilgangskontrollException(createPep1gDenyReason(safRequestContext, pep1Access), pep1Access);
			}

			AbacAnswer pep2Access = pep2.hasAccessWithAnswer(tilgangSak, safRequestContext);
			if (pep2Access.isDeny()) {
				throw new TilgangskontrollException(createPep2DenyReason(safRequestContext), pep2Access);
			}

			if (environment.getSelectionSet().contains(SELECTION_JOURNALPOST_DOKUMENTER)) {
				if (tilgangJournalpost.getJournalstatus() != MOTTATT) {
					pep2d.hasAccess(tilgangSak, safRequestContext);
					pep7d.hasAccess(tilgangSak, safRequestContext);
				}
			}

			AbacAnswer pep3Access = pep3.hasAccessWithAnswer(tilgangSak, safRequestContext);
			if (pep3Access.isDeny()) {
				throw new TilgangskontrollException(createPep3DenyReason(safRequestContext), pep3Access);
			}

			AbacAnswer pep4Access = pep4.hasAccessWithAnswer(tilgangJournalpost, safRequestContext);
			if (pep4Access.isDeny()) {
				throw new TilgangskontrollException(createPep4DenyReason(safRequestContext), pep4Access);
			}

			if (environment.getSelectionSet().contains(SELECTION_JOURNALPOST_DOKUMENTER)) {
				tilgangJournalpost.getDokumenter().forEach(tilgangDokumentInfo -> {
					pep5.hasAccess(tilgangDokumentInfo, safRequestContext);
					tilgangDokumentInfo.getTilgangDokumentvarianter().forEach(tilgangDokumentvariant ->
							pep6d.hasAccess(tilgangDokumentvariant, safRequestContext));
				});
			}
			return mapJournalpost(journalpostHolder.arkivJournalpost(), safRequestContext.getRequestCache());
		} catch (JournalpostIkkeFunnetException e) {
			throw new SafFunctionalException("Fant ikke journalpost i fagarkivet. " + errLog(journalpostId, eksternReferanseId), NOT_FOUND
			);
		}
	}

	private String errLog(String journalpostId, String eksternReferanseId) {
		return (isBlank(journalpostId) ? "eksternReferanseId=" : "journalpostId=") + (isBlank(journalpostId) ? eksternReferanseId : journalpostId);
	}
}
