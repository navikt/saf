package no.nav.saf.anticorruptionlayer.joark.domain;

import no.nav.saf.anticorruptionlayer.aktoer.TilgangsbrukerMapper;
import no.nav.saf.anticorruptionlayer.pdl.PdlResponse;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TilgangsbrukerMapperTest {

	private static final String FOLKEREGISTERIDENT = "11111111111";
	private static final String AKTOERID = "1313131313131";
	private static final String HISTORISK_FOLKEREGISTERIDENT = "22222222222";
	private static final String HISTORISK_AKTOERID = "3131313131313";
	private static final String NP_ID = "npId-111";

	@Test
	void shouldGetIdenterWhenNoHistorisk() {
		List<PdlResponse.PdlIdent> responseTo = createBaseResponse();
		TilgangBruker tilgangBruker = TilgangsbrukerMapper.map(responseTo);

		assertThat(tilgangBruker.getAktoerId()).isEqualTo(AKTOERID);
		assertThat(tilgangBruker.getFoedselsnr()).isEqualTo(FOLKEREGISTERIDENT);
		assertThat(tilgangBruker.getHistoriskeIdenter()).isEmpty();
		assertThat(tilgangBruker.getAlleIdenter()).contains(FOLKEREGISTERIDENT);
		assertThat(tilgangBruker.hentAlleAktoerId()).hasSize(1).contains(AKTOERID);
	}

	@Test
	void shouldGetIdenterWhenHistorisk() {
		List<PdlResponse.PdlIdent> baseResponse = createBaseResponse();
		baseResponse.addAll(Arrays.asList(
				createIdent(HISTORISK_FOLKEREGISTERIDENT, PdlResponse.PdlGruppe.FOLKEREGISTERIDENT, true),
				createIdent(HISTORISK_AKTOERID, PdlResponse.PdlGruppe.AKTORID, true)
		));
		TilgangBruker tilgangBruker = TilgangsbrukerMapper.map(baseResponse);

		assertThat(tilgangBruker.getAktoerId()).isEqualTo(AKTOERID);
		assertThat(tilgangBruker.getFoedselsnr()).isEqualTo(FOLKEREGISTERIDENT);
		assertThat(tilgangBruker.getHistoriskeIdenter()).hasSize(2);
		assertThat(tilgangBruker.getAlleIdenter()).contains(FOLKEREGISTERIDENT, HISTORISK_FOLKEREGISTERIDENT);
		assertThat(tilgangBruker.hentAlleAktoerId()).hasSize(2).contains(AKTOERID, HISTORISK_AKTOERID);
	}

	@Test
	void shouldMapNpIdToFoedselsnr() {
		List<PdlResponse.PdlIdent> response = List.of(createIdent(NP_ID, PdlResponse.PdlGruppe.NPID, false));
		TilgangBruker tilgangBruker = TilgangsbrukerMapper.map(response);

		assertThat(tilgangBruker.getFoedselsnr()).isEqualTo(NP_ID);
		assertThat(tilgangBruker.getAlleIdenter()).hasSize(1).contains(NP_ID);
	}

	List<PdlResponse.PdlIdent> createBaseResponse() {
		List<PdlResponse.PdlIdent> response = new ArrayList<>();
		response.add(createIdent(FOLKEREGISTERIDENT, PdlResponse.PdlGruppe.FOLKEREGISTERIDENT, false));
		response.add(createIdent(AKTOERID, PdlResponse.PdlGruppe.AKTORID, false));
		return response;
	}

	PdlResponse.PdlIdent createIdent(final String ident, final PdlResponse.PdlGruppe gruppe, final boolean historisk) {
		PdlResponse.PdlIdent pdlIdent = new PdlResponse.PdlIdent();
		pdlIdent.setIdent(ident);
		pdlIdent.setGruppe(gruppe);
		pdlIdent.setHistorisk(historisk);
		return pdlIdent;
	}

}
