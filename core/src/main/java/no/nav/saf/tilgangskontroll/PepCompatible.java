package no.nav.saf.tilgangskontroll;

import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;

public interface PepCompatible {
	TilgangBruker tilgangBruker();
	TilgangSak tilgangSak();
	TilgangJournalpost tilgangJournalpost();
	TilgangDokumentInfo tilgangDokumentInfo();
}
