package no.nav.saf.hentdokument;

import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;

record HentDokumentTilgang(TilgangBruker tilgangBruker, TilgangSak tilgangSak, TilgangJournalpost tilgangJournalpost) {
	TilgangDokumentInfo tilgangDokumentInfo() {
		if(tilgangJournalpost.getDokumenter() == null) {
			return null;
		}
		if (tilgangJournalpost.getDokumenter().isEmpty()) {
			return null;
		}
		return tilgangJournalpost.getDokumenter().get(0);
	}

	TilgangDokumentvariant tilgangDokumentvariant() {
		TilgangDokumentInfo tilgangDokumentInfo = tilgangDokumentInfo();
		if (tilgangDokumentInfo == null) {
			return null;
		}
		if (tilgangDokumentInfo.getTilgangDokumentvarianter() == null) {
			return null;
		}
		if (tilgangDokumentInfo.getTilgangDokumentvarianter().isEmpty()) {
			return null;
		}
		return tilgangDokumentInfo.getTilgangDokumentvarianter().get(0);
	}
}
