package no.nav.saf.anticorruptionlayer.pensjonsak;

import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tjeneste.visningsmodell.Sak;
import no.nav.saf.tjeneste.visningsmodell.kode.Tema;

import java.util.List;

public interface PensjonSakAntiCorruptionLayer {
	List<Arkivsak> findArkivsaker(String foedselsnummer, List<Tema> tema);

	List<TilgangSak> hentTilgangSakList(String personident);

	List<Sak> hentSakerByFoedselsnummer(String foedselsnummer);
}
