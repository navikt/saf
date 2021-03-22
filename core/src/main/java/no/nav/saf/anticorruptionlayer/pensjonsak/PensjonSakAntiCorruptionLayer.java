package no.nav.saf.anticorruptionlayer.pensjonsak;

import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;

import java.util.List;

public interface PensjonSakAntiCorruptionLayer {
	List<Arkivsak> findArkivsaker(TilgangBruker tilgangBruker, List<Tema> tema);

	List<Arkivsak> findArkivsaker(TilgangBruker tilgangBruker);

	String findFoedselsnummerBySakId(String sakId);
}
