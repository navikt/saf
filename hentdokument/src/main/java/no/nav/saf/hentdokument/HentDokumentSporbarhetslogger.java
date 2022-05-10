package no.nav.saf.hentdokument;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.exceptions.HentdokumentTilgangskontrollException;
import no.nav.saf.tilgangskontroll.SafRequestContext;

import static no.nav.saf.hentdokument.HentDokumentSporingLogglinje.BESLUTNING_DENY;
import static no.nav.saf.hentdokument.HentDokumentSporingLogglinje.BESLUTNING_PERMIT;

/**
 * Sporingslogger til nais.audit.
 * https://confluence.adeo.no/display/BOA/saf+-+Sporingslogg+hentdokument
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j(topic = "hentdokument_sporbarhetslogg")
class HentDokumentSporbarhetslogger {
	private static final Tema UKJENT_TEMA = Tema.UKJ;
	private static final String UKJENT_BRUKERID = "ukjent";

	void logPermit(final String journalpostId, final String dokumentInfoId, final String variantFormat,
				   TilgangSak tilgangSak, TilgangBruker tilgangBruker, final SafRequestContext safRequestContext) {
		logAccess(HentDokumentSporingLogglinje.builder()
				.brukerId(getBrukerId(tilgangBruker))
				.navIdent(safRequestContext.getUserId())
				.tilgangsbeslutning(BESLUTNING_PERMIT)
				.journalpostId(journalpostId)
				.dokumentInfoId(dokumentInfoId)
				.variantformat(variantFormat)
				.tema(getTema(tilgangSak))
				.build());
	}

	void logDeny(final String journalpostId, final String dokumentInfoId, final String variantFormat,
				 TilgangSak tilgangSak, TilgangBruker tilgangBruker, final SafRequestContext safRequestContext, HentdokumentTilgangskontrollException e) {
		logAccess(HentDokumentSporingLogglinje.builder()
				.brukerId(getBrukerId(tilgangBruker))
				.navIdent(safRequestContext.getUserId())
				.tilgangsbeslutning(BESLUTNING_DENY)
				.begrunnelse(e.getDenyReason())
				.journalpostId(journalpostId)
				.dokumentInfoId(dokumentInfoId)
				.variantformat(variantFormat)
				.tema(getTema(tilgangSak))
				.build());
	}

	private void logAccess(HentDokumentSporingLogglinje hentDokumentSporingLogglinje) {
		log.info(hentDokumentSporingLogglinje.toString());
	}

	private String getTema(TilgangSak tilgangSak) {
		if (tilgangSak == null) {
			return UKJENT_TEMA.name();
		} else {
			return tilgangSak.getTema() == null ? UKJENT_TEMA.name() : tilgangSak.getTema().name();
		}
	}

	private String getBrukerId(TilgangBruker tilgangBruker) {
		if (tilgangBruker == null) {
			return UKJENT_BRUKERID;
		}
		if (tilgangBruker.isPerson()) {
			return tilgangBruker.getFoedselsnr();
		} else {
			return tilgangBruker.getOrgnummer();
		}
	}
}
