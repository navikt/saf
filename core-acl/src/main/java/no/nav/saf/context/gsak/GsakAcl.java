package no.nav.saf.context.gsak;

import no.nav.saf.context.saf.domain.Sak;
import no.nav.saf.context.saf.domain.Tema;
import no.nav.saf.context.saf.domain.kode.Temakode;

import java.util.List;
import java.util.Set;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface GsakAcl {
	Set<Tema> findTemaByAktoerIdAndFilterTemakode(String aktoerId, List<Temakode> temakoder);
	List<Sak> findSakByAktoerIdAndTemakode(String aktoerId, Temakode temakode);
}
