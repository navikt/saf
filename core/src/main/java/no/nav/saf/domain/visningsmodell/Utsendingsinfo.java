package no.nav.saf.domain.visningsmodell;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Utsendingsinfo {

	EpostSendt epostSendt;
	SmsSendt smsSendt;
	FysiskpostSendt fysiskpostSendt;
	DigitalpostSendt digitalpostSendt;

	@Value
	@Builder
	public static class EpostSendt {
		String tittle;
		String adresse;
		String varslingstekst;
	}

	@Value
	@Builder
	public static class SmsSendt {
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
