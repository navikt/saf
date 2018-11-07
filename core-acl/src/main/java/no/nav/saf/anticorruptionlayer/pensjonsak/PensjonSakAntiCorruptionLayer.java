package no.nav.saf.anticorruptionlayer.pensjonsak;

import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tjeneste.visningsmodell.Sak;

import java.util.List;

public interface PensjonSakAntiCorruptionLayer {
	List<TilgangSak> hentTilgangSakList(String personident);
	List<Sak> hentSakerByFoedselsnummer(String foedselsnummer);
}
