package no.nav.saf.anticorruptionlayer.gsak;

import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tjeneste.visningsmodell.Sak;
import no.nav.saf.tjeneste.visningsmodell.kode.Tema;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface GsakAntiCorruptionLayer {

	List<Sak> findSakerByAktoerId(String aktoerId);

	List<Arkivsak> findArkivsaker(String aktoerId, List<Tema> tema);

	List<TilgangSak> findTilgangSakListByAktoerId(String aktoerId, List<Tema> tema);

	TilgangBruker findTilgangSakBySakId(String sakId);
}
