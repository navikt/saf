package no.nav.saf.hentdokument;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;

import java.util.Optional;

record HentDokumentTilgang(TilgangBruker tilgangBruker,
						   TilgangSak tilgangSak,
						   TilgangJournalpost tilgangJournalpost,
						   VariantFormatCode variantFormat) {

	TilgangDokumentInfo tilgangDokumentInfo() {
		if(tilgangJournalpost.getDokumenter() == null) {
			return null;
		}
		if (tilgangJournalpost.getDokumenter().isEmpty()) {
			return null;
		}
		return tilgangJournalpost.getDokumenter().get(0);
	}

	Optional<TilgangDokumentvariant> tilgangDokumentvariant() {
		TilgangDokumentInfo tilgangDokumentInfo = tilgangDokumentInfo();
		if (tilgangDokumentInfo == null) {
			return Optional.empty();
		}
		if (tilgangDokumentInfo.getTilgangDokumentvarianter() == null) {
			return Optional.empty();
		}
		if (tilgangDokumentInfo.getTilgangDokumentvarianter().isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(tilgangDokumentInfo.getTilgangDokumentvarianter().get(0));
	}
}
