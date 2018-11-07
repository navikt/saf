package no.nav.saf.anticorruptionlayer.gsak;

import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tjeneste.visningsmodell.Sak;
import no.nav.saf.tjeneste.visningsmodell.Tema;
import no.nav.saf.tjeneste.visningsmodell.kode.Temakode;

import java.util.List;
import java.util.Set;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface GsakAntiCorruptionLayer {

	Set<Tema> findTemaerByAktoerIdAndFilterTemakode(String aktoerId, List<Temakode> temakoder);

	List<Sak> findSakerByAktoerId(final String aktoerId, final List<Temakode> temakodeFilter);

	List<Sak> findSakerByAktoerId(final String aktoerId);

	List<TilgangSak> findTilgangSakListByAktoerId(final String aktoerId);
}
