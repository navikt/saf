package no.nav.saf.domain.visningsmodell;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UtsendingsInfo {

	FysiskPostadresse fysiskPostadresse;

	DigitalPostadresse digitalPostadresse;

	NavNoVarsling navNoVarsling;

	@Value
	@Builder
	public static class FysiskPostadresse {
		String adresselinje1;
		String adresselinje2;
		String adresselinje3;
		String postnummer;
		String poststed;
		String landkode;
	}

	@Value
	@Builder
	public static class DigitalPostadresse {
		String adresse;
		String postkasseLeverandor;
	}

	@Value
	@Builder
	public static class NavNoVarsling {
		String kontaktinformasjon;
		String varslingstekst;
	}
}
