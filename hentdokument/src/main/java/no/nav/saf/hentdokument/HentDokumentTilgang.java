package no.nav.saf.hentdokument;

import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;

record HentDokumentTilgang(TilgangBruker tilgangBruker, TilgangSak tilgangSak, TilgangJournalpost tilgangJournalpost) {
	TilgangDokumentInfo tilgangDokumentInfo() {
		return tilgangJournalpost.getDokumenter().get(0);
	}

	TilgangDokumentvariant tilgangDokumentvariant() {
		return tilgangDokumentInfo().getTilgangDokumentvarianter().get(0);
	}
}
