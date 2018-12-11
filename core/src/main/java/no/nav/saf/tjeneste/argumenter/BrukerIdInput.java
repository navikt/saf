package no.nav.saf.tjeneste.argumenter;

import lombok.Value;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Value
public class BrukerIdInput {
	private String id;
	private BrukerIdType idType;

	public boolean isPersonBruker(){
		return !idType.equals(BrukerIdType.ORGNR);
	}
}
