package no.nav.saf.anticorruptionlayer.joark.domain;

import no.nav.saf.anticorruptionlayer.aktoer.TilgangsbrukerMapper;
import no.nav.saf.anticorruptionlayer.pdl.PdlResponse;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TilgangsbrukerMapperTest {

	@Test
	public void testEmptyList(){
		List<PdlResponse.PdlIdent> responseList = new ArrayList<>();
		TilgangBruker result = TilgangsbrukerMapper.map(responseList, "aktoer", "fnr");
		assertNotNull(result);
		List<String> ident = result.getAlleIdenter();
		assertNotNull(ident);
		List<String> fnr = result.hentAlleFodselsnummer();
		assertNotNull(fnr);
		List<String> aktoerId = result.hentAlleAktoerId();
		assertNotNull(aktoerId);

	}

	@Test
	public void testFnrList(){
		List<PdlResponse.PdlIdent> responseList = new ArrayList<>();
		PdlResponse.PdlIdent pdlIdent = new PdlResponse.PdlIdent();
		pdlIdent.setIdent("test");
		pdlIdent.setHistorisk(true);
	 	pdlIdent.setGruppe(PdlResponse.PdlGruppe.AKTORID);
	 	responseList.add(pdlIdent);
		TilgangBruker result = TilgangsbrukerMapper.map(responseList, "aktoer", "fnr");
		assertNotNull(result);
		List<String> ident = result.getAlleIdenter();
		assertNotNull(ident);
		List<String> fnr = result.hentAlleFodselsnummer();
		assertNotNull(fnr);
		List<String> aktoerId = result.hentAlleAktoerId();
		assertNotNull(aktoerId);

	}

}
