package no.nav.saf.anticorruptionlayer.gsak;

import no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.Sak;
import no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.Tema;
import no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.kode.Temakode;

import java.util.List;
import java.util.Set;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface GsakAntiCorruptionLayer {
	Set<Tema> findTemaByAktoerIdAndFilterTemakode(String aktoerId, List<Temakode> temakoder);
	List<Sak> findSakByAktoerIdAndTemakode(String aktoerId, Temakode temakode);
}
