package no.nav.saf.anticorruptionlayer.aktoer;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.pdl.PdlResponse;
import no.nav.saf.domain.tilgangsmodell.IdentType;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangIdent;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static no.nav.saf.anticorruptionlayer.pdl.PdlResponse.PdlGruppe.*;

@Slf4j
public class TilgangsbrukerMapper {

	private TilgangsbrukerMapper() {
	}

	private static final EnumSet<PdlResponse.PdlGruppe> GYLDIGE_IDENTGRUPPER = EnumSet.of(
			AKTORID,
			FOLKEREGISTERIDENT,
			NPID);

	public static TilgangBruker map(List<PdlResponse.PdlIdent> responseTo) {
		String aktoerId = null;
		String foedselsnummer = null;
		List<TilgangIdent> tilgangsIdentList = new ArrayList<>();
		for (PdlResponse.PdlIdent pdlIdent : responseTo) {
			if (pdlIdent.isHistorisk() && GYLDIGE_IDENTGRUPPER.contains(pdlIdent.getGruppe())) {
				tilgangsIdentList.add(TilgangIdent.builder()
						.identifikator(pdlIdent.getIdent())
						.identType(AKTORID.equals(pdlIdent.getGruppe()) ? IdentType.AKTOERID : IdentType.FOLKEREGISTERIDENT)
						.build());
			} else if (AKTORID.equals(pdlIdent.getGruppe())) {
				aktoerId = pdlIdent.getIdent();
			} else if (FOLKEREGISTERIDENT.equals(pdlIdent.getGruppe())) {
				foedselsnummer = pdlIdent.getIdent();
			} else if (NPID.equals(pdlIdent.getGruppe()) && foedselsnummer == null) {
				foedselsnummer = pdlIdent.getIdent();
			}
		}

		if (foedselsnummer == null) {
			log.warn("Feil i mapping av identer fra Pdl. foedselsnummer er null etter mapping.");
		}

		if (aktoerId == null) {
			log.error("Feil i mapping av identer fra Pdl. aktoerId er null etter mapping. Må følges opp.");
		}

		return TilgangBruker.builder()
				.foedselsnr(foedselsnummer)
				.aktoerId(aktoerId)
				.historiskeIdenter(tilgangsIdentList)
				.build();
	}
}
