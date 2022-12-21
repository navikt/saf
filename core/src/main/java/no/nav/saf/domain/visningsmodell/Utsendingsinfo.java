package no.nav.saf.domain.visningsmodell;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Utsendingsinfo {
	String adresselinje1;
	String adresselinje2;
	String adresselinje3;
	String postnummer;
	String poststed;
	String landkode;
	String digitalpostkasseAdresse;
	String varselSendtTil;
	String varseltekst;
}
