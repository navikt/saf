package no.nav.saf.anticorruptionlayer.pensjonsak;

import no.nav.saf.anticorruptionlayer.pensjonsak.domain.TilgangSak;

import java.util.List;

public interface PensjonSakAntiCorruptionLayer {
	List<TilgangSak> hentSakSammendrag(String personident);
}
