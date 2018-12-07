package no.nav.saf.anticorruptionlayer.gsak;

import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tjeneste.visningsmodell.kode.Tema;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface GsakAntiCorruptionLayer {

	List<Arkivsak> findArkivsaker(String aktoerId, List<Tema> tema);

	TilgangSak findTilgangSakBySakId(String sakId);

	List<Arkivsak> findTilgangSakListByFagsakIdAndFagsaksystem(String fagsakId, String fagsaksystem, List<Tema> tema);

	List<String> findAktoerIdListByFagsakIdAndFagsaksystem(String fagsakId, String fagsaksystem);

	TilgangBruker findTilgangBrukerBySakId(String sakId);
}
