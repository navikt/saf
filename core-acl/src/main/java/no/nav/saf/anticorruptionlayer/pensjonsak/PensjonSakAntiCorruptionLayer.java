package no.nav.saf.anticorruptionlayer.pensjonsak;

import no.nav.saf.domain.tilgangsmodell.TilgangSak;

import java.util.List;

public interface PensjonSakAntiCorruptionLayer {
	List<TilgangSak> hentTilgangSakList(String personident);
}
