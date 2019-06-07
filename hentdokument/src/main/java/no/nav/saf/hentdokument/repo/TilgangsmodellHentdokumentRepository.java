package no.nav.saf.hentdokument.repo;

import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public interface TilgangsmodellHentdokumentRepository {

	TilgangJournalpost findTilgangJournalpostFromSafRequestContext(SafRequestContext safRequestContext);

	TilgangBruker findTilgangBruker(Arkivsak arkivsak, SafRequestContext safRequestContext);

	TilgangBruker findTilgangBrukerBySakId(String sakId, Arkivsakssystem arkivsaksystem, SafRequestContext safRequestContext);

	Arkivsak findArkivsakAndCacheJournalpostDto(String journalpostId, String dokumentInfoId, String variantFormat, SafRequestContext safRequestContext);

	TilgangSak findTilgangSak(Arkivsak arkivsak, TilgangBruker tilgangBruker, SafRequestContext safRequestContext);

}
