package no.nav.saf.domain.visningsmodell;

import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.List;

@Value
@Builder
public class Utsendingsinfo {

	List<VarselSendt> varselSendt;
	EpostVarselSendt epostVarselSendt;
	SmsVarselSendt smsVarselSendt;
	FysiskpostSendt fysiskpostSendt;
	DigitalpostSendt digitalpostSendt;

	@Value
	@Builder
	public static class VarselSendt {

		String type;
		String tittel;
		String adresse;
		String varslingstekst;
		LocalDateTime varslingstidspunkt;

		public static VarselSendtBuilder epost() {
			return builder().type("EPOST");
		}

		public static VarselSendtBuilder sms() {
			return builder().type("SMS");
		}
	}

	@Value
	@Builder
	public static class EpostVarselSendt {
		String tittel;
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
	}
}
