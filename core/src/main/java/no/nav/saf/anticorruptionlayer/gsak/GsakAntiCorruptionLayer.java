package no.nav.saf.anticorruptionlayer.gsak;

import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;

import java.util.List;
import java.util.Map;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public interface GsakAntiCorruptionLayer {

	List<Arkivsak> findArkivsakerByAktoerId(String aktoerId, List<Tema> tema);

	List<Arkivsak> findArkivsakerByOrgnr(String orgnr, List<Tema> tema);

	Arkivsak findArkivsakBySakId(String sakId);

	List<Arkivsak> findTilgangSakListByFagsakIdAndFagsaksystem(String fagsakId, String fagsaksystem, List<Tema> tema);

	Map<String, List<String>> findIdListsByFagsakIdAndFagsaksystem(String fagsakId, String fagsaksystem);

	TilgangBruker findTilgangBrukerBySakId(String sakId);
}
