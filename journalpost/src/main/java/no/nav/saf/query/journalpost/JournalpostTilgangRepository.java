package no.nav.saf.query.journalpost;

import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;

interface JournalpostTilgangRepository {
	TilgangJournalpost findTilgangJournalpostFromSafRequestContext(SafRequestContext safRequestContext, TilgangSak tilgangSak);

	TilgangBruker findTilgangBruker(Arkivsak arkivsak, SafRequestContext safRequestContext);

	TilgangBruker findTilgangBrukerByArkivsak(Arkivsak arkivsak);

	Arkivsak findArkivsakAndCacheJournalpostDto(String journalpostId, SafRequestContext safRequestContext);

	TilgangSak findTilgangSak(Arkivsak arkivsak, TilgangBruker tilgangBruker, SafRequestContext safRequestContext);
}
