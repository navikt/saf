package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtsendingsInfoDto {

	FysiskPostadresseDto fysiskPostadresse;
	DigitalPostadresseDto digitalPostadresse;
	NavNoVarslingDto navNoVarsling;
	List<EpostVarselDto> epostVarsel;
	List<SmsVarselDto> smsVarsel;

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
		String digitalpostkasseAdresse;
		String postkasseLeverandor;
	}

	@Value
	@Builder
	public static class NavNoVarslingDto {
		String varselSendtTil;
		String varseltekst;
	}

	@Value
	@Builder
	public static class EpostVarselDto {
		String tittel;
		String tekst;
		String epostadresse;
		LocalDateTime varslingstidspunkt;
	}

	@Value
	@Builder
	public static class SmsVarselDto {
		String tekst;
		String mobilnummer;
		LocalDateTime varslingstidspunkt;
	}
}