package no.nav.saf.hentdokument;

import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.PepCompatible;

public record HentDokumentTilgang(TilgangBruker tilgangBruker, TilgangSak tilgangSak, TilgangJournalpost tilgangJournalpost, TilgangDokumentInfo tilgangDokumentInfo) implements PepCompatible {

	@Override
	public TilgangBruker tilgangBruker() {
		return tilgangBruker;
	}

	@Override
	public TilgangSak tilgangSak() {
		return tilgangSak;
	}

	@Override
	public TilgangJournalpost tilgangJournalpost() {
		return tilgangJournalpost;
	}

	@Override
	public TilgangDokumentInfo tilgangDokumentInfo() {
		return tilgangDokumentInfo;
	}
}
