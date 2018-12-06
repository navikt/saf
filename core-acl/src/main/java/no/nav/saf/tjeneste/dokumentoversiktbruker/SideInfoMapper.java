package no.nav.saf.tjeneste.dokumentoversiktbruker;

import no.nav.saf.tjeneste.visningsmodell.Journalpost;
import no.nav.saf.tjeneste.visningsmodell.SideInfo;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class SideInfoMapper {
	public SideInfo mapSideInfo(DokumentoversiktBrukerArguments arguments, List<Journalpost> journalposter) {
		return new SideInfo(
				sluttpeker(journalposter),
				finnesNesteSide(arguments, journalposter),
				journalposter.isEmpty() ? null : journalposter.get(0).getPeker(),
				finnesForrigeSide(arguments, journalposter)
		);
	}

	private String sluttpeker(List<Journalpost> journalposter) {
		if (journalposter.isEmpty()) {
			return null;
		} else {
			return journalposter.get(journalposter.size() - 1).getPeker();
		}
	}

	private boolean finnesNesteSide(DokumentoversiktBrukerArguments arguments, List<Journalpost> journalposter) {
		if (arguments.getFoerste() != null) {
			return journalposter.size() > arguments.getFoerste();
		} else if (arguments.getFoerPeker() != null) {
			// TODO
			return false;
		}
		return false;
	}

	private boolean finnesForrigeSide(DokumentoversiktBrukerArguments arguments, List<Journalpost> journalposter) {
		if (arguments.getSiste() != null) {
			return journalposter.size() > arguments.getSiste();
		} else if (arguments.getEtterPeker() != null) {
			//TODO
			return false;
		}
		return false;
	}
}
