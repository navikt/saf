package no.nav.saf.legacycontext.gsak;

import no.nav.saf.domain.visningsmodell.Sak;
import no.nav.saf.domain.visningsmodell.Tema;
import no.nav.saf.domain.visningsmodell.kode.Temakode;

import java.util.List;
import java.util.Set;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface GsakAcl {
	Set<Tema> findTemaByAktoerIdAndFilterTemakode(String aktoerId, List<Temakode> temakoder);
	List<Sak> findSakByAktoerIdAndTemakode(String aktoerId, Temakode temakode);
}
