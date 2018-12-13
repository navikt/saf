package no.nav.saf.tjeneste.argumenter;

import lombok.Value;
import no.nav.saf.tjeneste.visningsmodell.BrukerIdType;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Value
public class BrukerIdInput {
	private String id;
	private BrukerIdType type;

	public boolean isPersonBruker(){
		return !type.equals(BrukerIdType.ORGNR);
	}
}
