package no.nav.saf.anticorruptionlayer.gsak;

import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tjeneste.visningsmodell.Sak;
import no.nav.saf.tjeneste.visningsmodell.kode.Temakode;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface GsakAntiCorruptionLayer {

	List<Sak> findSakerByAktoerId(final String aktoerId, final List<Temakode> temakodeFilter);

	List<Sak> findSakerByAktoerId(final String aktoerId);

	List<TilgangSak> findTilgangSakListByAktoerId(final String aktoerId);

	TilgangBruker findTilgangSakBySakId(final String sakId);
}
