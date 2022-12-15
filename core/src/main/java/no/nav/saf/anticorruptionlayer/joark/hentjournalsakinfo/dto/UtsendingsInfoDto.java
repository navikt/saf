package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtsendingsInfoDto {

	FysiskPostadresseDto fysiskPostadresse;
	DigitalPostadresseDto digitalPostadresse;
	NavNoVarslingDto navNoVarsling;

	@Value
	@Builder
	public static class FysiskPostadresseDto {
		String adresselinje1;
		String adresselinje2;
		String adresselinje3;
		String postnummer;
		String poststed;
		String landkode;
	}

	@Value
	@Builder
	public static class DigitalPostadresseDto {
		String adresse;
		String postkasseLeverandor;
	}

	@Value
	@Builder
	public static class NavNoVarslingDto {
		String kontaktinformasjon;
		String varslingstekst;
	}
}
