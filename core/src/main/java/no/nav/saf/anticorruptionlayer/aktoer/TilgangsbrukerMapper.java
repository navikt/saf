package no.nav.saf.anticorruptionlayer.aktoer;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.pdl.PdlResponse;
import no.nav.saf.domain.tilgangsmodell.IdentType;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangIdent;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

@Slf4j
public class TilgangsbrukerMapper {

	private TilgangsbrukerMapper() {
	}

	private static final EnumSet<PdlResponse.PdlGruppe> GYLDIGE_IDENTGRUPPER = EnumSet.of(
			PdlResponse.PdlGruppe.AKTORID,
			PdlResponse.PdlGruppe.FOLKEREGISTERIDENT,
			PdlResponse.PdlGruppe.NPID);

	public static TilgangBruker map(List<PdlResponse.PdlIdent> responseTo) {
		String aktoerId = null;
		String foedselsnummer = null;
		String npId = null;
		List<TilgangIdent> tilgangsIdentList = new ArrayList<>();
		for (PdlResponse.PdlIdent pdlIdent : responseTo) {
			if (pdlIdent.isHistorisk() && GYLDIGE_IDENTGRUPPER.contains(pdlIdent.getGruppe())) {
				tilgangsIdentList.add(TilgangIdent.builder()
						.identifikator(pdlIdent.getIdent())
						.identType(IdentType.fromPdlGruppe(pdlIdent.getGruppe()))
						.build());
			} else if (pdlIdent.getGruppe().equals(PdlResponse.PdlGruppe.AKTORID)) {
				aktoerId = pdlIdent.getIdent();
			} else if (pdlIdent.getGruppe().equals(PdlResponse.PdlGruppe.FOLKEREGISTERIDENT)) {
				foedselsnummer = pdlIdent.getIdent();
			} else if (pdlIdent.getGruppe().equals(PdlResponse.PdlGruppe.NPID)) {
				npId = pdlIdent.getIdent();
			}
		}

		if (aktoerId == null) {
			log.error("Feil i mapping av identer fra Pdl. aktoerId er null etter mapping. Må følges opp.");
		}

		return TilgangBruker.builder()
				.foedselsnr(foedselsnummer)
				.aktoerId(aktoerId)
				.npId(npId)
				.historiskeIdenter(tilgangsIdentList)
				.build();
	}
}
