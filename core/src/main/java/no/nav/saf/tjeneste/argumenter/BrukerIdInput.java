package no.nav.saf.tjeneste.argumenter;

import lombok.Value;
import no.nav.saf.domain.visningsmodell.BrukerIdType;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Value
public class BrukerIdInput {
	private String id;
	private BrukerIdType type;

	public boolean isPersonBruker() {
		return !type.equals(BrukerIdType.ORGNR);
	}

	@Override
	public String toString() {
		switch (type) {
			case AKTOERID:
				return "aktørId=" + id;
			case FNR:
				return "fødselsnummer=*****";
			case ORGNR:
				return "organisasjonsnummer=" + id;
			default:
				return "ukjentId=" + id;
		}
	}
}
