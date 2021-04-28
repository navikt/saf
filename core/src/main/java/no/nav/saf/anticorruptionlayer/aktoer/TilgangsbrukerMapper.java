package no.nav.saf.anticorruptionlayer.aktoer;

import no.nav.saf.anticorruptionlayer.pdl.PdlResponse;
import no.nav.saf.domain.tilgangsmodell.IdentType;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangIdent;

import java.util.ArrayList;
import java.util.List;

public class TilgangsbrukerMapper {

	public static TilgangBruker map(List<PdlResponse.PdlIdent> responseTo, String aktoerId, String foedselsnummer) {
		List<TilgangIdent> tilgangsIdentList = new ArrayList<>();
		for(PdlResponse.PdlIdent pdlIdent : responseTo){
			if(pdlIdent.isHistorisk()){
				tilgangsIdentList.add(TilgangIdent.builder()
						.identifikator(pdlIdent.getIdent())
						.identType(pdlIdent.getGruppe().equals(PdlResponse.PdlGruppe.AKTORID)? IdentType.AKTOERID:IdentType.FOLKEREGISTERIDENT)
						.build());
			}else if (pdlIdent.getGruppe().equals(PdlResponse.PdlGruppe.AKTORID)){
				aktoerId = pdlIdent.getIdent();
			}else if (pdlIdent.getGruppe().equals(PdlResponse.PdlGruppe.FOLKEREGISTERIDENT)){
				foedselsnummer = pdlIdent.getIdent();
			}
		}
		return TilgangBruker.builder()
				.foedselsnr(foedselsnummer)
				.aktoerId(aktoerId)
				.historiskeIdenter(tilgangsIdentList)
				.build();
	}
}
