package no.nav.saf.tjeneste.argumenter;

import lombok.Value;
import no.nav.saf.domain.visningsmodell.BrukerIdType;

@Value
public class BrukerIdInput {
	String id;
	BrukerIdType type;

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
