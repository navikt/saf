package no.nav.saf.query.journalpost;

import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;

public record JournalpostTilgang(TilgangBruker tilgangBruker, TilgangSak tilgangSak, TilgangJournalpost tilgangJournalpost) {
}
