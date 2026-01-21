package no.nav.saf.tjeneste.argumenter;

import lombok.Value;
import no.nav.saf.domain.visningsmodell.BrukerIdType;

@Value
public class BrukerIdInput {
	String id;
	BrukerIdType type;

	@Override
	public String toString() {
		return switch (type) {
			case AKTOERID -> "aktørId";
			case FNR -> "fødselsnummer";
			case ORGNR -> "organisasjonsnummer=" + id;
		};
	}
}
