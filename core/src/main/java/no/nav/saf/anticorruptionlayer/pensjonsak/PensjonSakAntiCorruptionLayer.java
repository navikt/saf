package no.nav.saf.anticorruptionlayer.pensjonsak;

import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.tjeneste.visningsmodell.kode.Tema;

import java.util.List;

public interface PensjonSakAntiCorruptionLayer {
	List<Arkivsak> findArkivsaker(TilgangBruker tilgangBruker, List<Tema> tema);

	String findFoedselsnummerBySakId(String sakId);
}
