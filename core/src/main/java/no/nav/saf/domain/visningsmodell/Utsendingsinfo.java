package no.nav.saf.domain.visningsmodell;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Utsendingsinfo {

	EpostVarselSendt epostVarselSendt;
	SmsVarselSendt smsVarselSendt;
	FysiskpostSendt fysiskpostSendt;
	DigitalpostSendt digitalpostSendt;

	@Value
	@Builder
	public static class EpostVarselSendt {
		String tittle;
		String adresse;
		String varslingstekst;
	}

	@Value
	@Builder
	public static class SmsVarselSendt {
		String adresse;
		String varslingstekst;
	}

	@Value
	@Builder
	public static class FysiskpostSendt {
		String adressetekstKonvolutt;
	}

	@Value
	@Builder
	public static class DigitalpostSendt {
		String adresse;
		String leverandoer;
		String varslingstekst;
	}
}
