package no.nav.saf.anticorruptionlayer.pensjonSak;

import no.nav.saf.anticorruptionlayer.pensjonSak.domain.TilgangSak;

import java.util.List;

public interface PensjonSakAntiCorruptionLayer {
	List<TilgangSak> hentSakSammendrag(String personident);
}
