package no.nav.saf.anticorruptionlayer.joark;

import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivDigitalPostadresse;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivEpostVarsel;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivFysiskPostadresse;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivNavNoVarsling;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivSmsVarsel;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivUtsendingsInfo;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.Collections.emptyList;

public class ArkivUtsendingsInfoTestObjects {
	public static final String ADRESSELINJE1 = "adresselinje1";
	public static final String ADRESSELINJE2 = "adresselinje2";
	public static final String ADRESSELINJE3 = "adresselinje3";
	public static final String POSTNUMMER = "postnummer";
	public static final String POSTSTED = "poststed";
	public static final String LANDKODE = "landkode";
	public static final String FORVENTET_ADRESSETEKST_KONVOLUTT = """
			adresselinje1
			adresselinje2
			adresselinje3
			postnummer poststed
			landkode""";

	public static final String FORVENTET_ADRESSETEKST_KONVOLUTT_UTEN_LANDKODE_FOR_NORSKADRESSE = """
			adresselinje1
			adresselinje2
			postnummer poststed""";
	public static final String INGEN_POSTNUMMER_POSTSTED_FORVENTET_ADRESSETEKST_KONVOLUTT = """
			adresselinje1
			adresselinje2
			adresselinje3
			landkode""";
	public static final String KUN_POSTNUMMER_POSTSTED_LANDKODE_FORVENTET_ADRESSETEKST_KONVOLUTT = """
			postnummer poststed
			landkode""";


	public static final String SMS_OG_EPOST_VEDTAK_INPUT_VARSLINGSTEKST = "{\"epost\":\"Tittel Vedtak fra NAV, Tekst <!DOCTYPE html><html><head><title>Vedtak fra NAV</title></head><body><!DOCTYPE html>\\n<html>\\n\\t<head>\\n\\t\\t<title>Vedtak fra NAV</title>\\n\\t</head>\\n\\t<body>\\n\\t\\t<p>Hei!</p>\\n\\t\\t<p>Du har fått et vedtak fra NAV.</p>\\n\\t\\t<p>Logg inn på nav.no for å lese det.</p>\\n\\t\\t<p>Vennlig hilsen</p>\\n\\t\\t<p>NAV</p>\\n\\t</body>\\n</html></body></html>\\n\",\"sms\":\"Hei! Du har fått et vedtak fra NAV. Logg inn på nav.no for å lese det. Vennlig hilsen NAV\"}";
	public static final String SMS_OG_EPOST_VEDTAK_INPUT_ADRESSE = "{\"epost\":\"navn.navnesen@nav.no\",\"sms\":\"99999999\"}";

	public static final String EPOST_VEDTAK_INPUT_DIGITAL_KONTAKTINFO = "{\"epost\":\"navn.navnesen@nav.no\",\"sms\":null}";
	public static final String EPOST_VEDTAK_INPUT_VARSLINGSTEKST = "{\"epost\":\"Tittel Vedtak fra NAV, Tekst <!DOCTYPE html><html><head><title>Vedtak fra NAV</title></head><body><!DOCTYPE html>\\n<html>\\n\\t<head>\\n\\t\\t<title>Vedtak fra NAV</title>\\n\\t</head>\\n\\t<body>\\n\\t\\t<p>Hei!</p>\\n\\t\\t<p>Du har fått et vedtak fra NAV.</p>\\n\\t\\t<p>Logg inn på nav.no for å lese det.</p>\\n\\t\\t<p>Vennlig hilsen</p>\\n\\t\\t<p>NAV</p>\\n\\t</body>\\n</html></body></html>\\n\",\"sms\":null}";
	public static final String EPOST_VEDTAK_FORVENTET_ADRESSE = "navn.navnesen@nav.no";
	public static final String EPOST_VEDTAK_FORVENTET_TITTEL = "Vedtak fra NAV";
	public static final String EPOST_VEDTAK_FORVENTET_VARSLINGSTEKST = """
			<!DOCTYPE html><html><head><title>Vedtak fra NAV</title></head><body><!DOCTYPE html>
			<html>
				<head>
					<title>Vedtak fra NAV</title>
				</head>
				<body>
					<p>Hei!</p>
					<p>Du har fått et vedtak fra NAV.</p>
					<p>Logg inn på nav.no for å lese det.</p>
					<p>Vennlig hilsen</p>
					<p>NAV</p>
				</body>
			</html></body></html>
			""";
	public static final String EPOST_MELDING_MED_KOMMA_INPUT_DIGITAL_KONTAKTINFO = "{\"epost\":\"kommanavn.navnesen@nav.no\",\"sms\":null}";
	public static final String EPOST_MELDING_MED_KOMMA_INPUT_VARSLINGSTEKST = "{\"epost\":\"Tittel Melding med informasjon, fra NAV, Tekst <!DOCTYPE html><html><head><title>Melding med informasjon, fra NAV</title></head><body><!DOCTYPE html>\\n<html>\\n    <head>\\n        <title>Melding med informasjon, fra NAV</title>\\n    </head>\\n    <body>\\n        <p>Hei!</p>\\n        <p>Du har fått en melding, med informasjon, fra NAV.</p>\\n        <p>Logg inn på nav.no for å lese den.</p>\\n        <p>Vennlig hilsen</p>\\n        <p>NAV</p>\\n    </body>\\n</html></body></html>\\n\",\"sms\":null}";
	public static final String EPOST_MELDING_MED_KOMMA_FORVENTET_ADRESSE = "kommanavn.navnesen@nav.no";
	public static final String EPOST_MELDING_MED_KOMMA_FORVENTET_TITTEL = "Melding med informasjon, fra NAV";
	public static final String EPOST_MELDING_MED_KOMMA_FORVENTET_VARSLINGSTEKST = """
			<!DOCTYPE html><html><head><title>Melding med informasjon, fra NAV</title></head><body><!DOCTYPE html>
			<html>
			    <head>
			        <title>Melding med informasjon, fra NAV</title>
			    </head>
			    <body>
			        <p>Hei!</p>
			        <p>Du har fått en melding, med informasjon, fra NAV.</p>
			        <p>Logg inn på nav.no for å lese den.</p>
			        <p>Vennlig hilsen</p>
			        <p>NAV</p>
			    </body>
			</html></body></html>
			""";
	public static final String SMS_VEDTAK_INPUT_DIGITAL_KONTAKTINFO = "{\"epost\":null,\"sms\":\"99999999\"}";
	public static final String SMS_VEDTAK_INPUT_VARSLINGSTEKST = "{\"epost\":null,\"sms\":\"Hei! Du har fått et vedtak fra NAV. Logg inn på nav.no for å lese det. Vennlig hilsen NAV\"}";
	public static final String SMS_VEDTAK_FORVENTET_ADRESSE = "99999999";
	public static final String SMS_VEDTAK_FORVENTET_VARSLINGSTEKST = "Hei! Du har fått et vedtak fra NAV. Logg inn på nav.no for å lese det. Vennlig hilsen NAV";

	public static final String SMS_OG_EPOST_AVVIK_NULL_INPUT_DIGITAL_KONTAKTINFO = "{\"epost\":null,\"sms\":null}";
	public static final String SMS_OG_EPOST_AVVIK_NULL_INPUT_VARSLINGSTEKST = "{\"epost\":null,\"sms\":null}";

	public static final String DIGITALPOSTKASSE_ADRESSE = "tom.tom#2541";
	public static final LocalDateTime VARSLINGSTIDSPUNKT = LocalDateTime.now();

	public static ArkivUtsendingsInfo utsendingsInfoDtoWithFysiskPostadresse() {
		return ArkivUtsendingsInfo.builder()
				.fysiskPostadresse(ArkivFysiskPostadresse.builder()
						.adresselinje1(ADRESSELINJE1)
						.adresselinje2(ADRESSELINJE2)
						.adresselinje3(ADRESSELINJE3)
						.postnummer(POSTNUMMER)
						.poststed(POSTSTED)
						.landkode(LANDKODE)
						.build())
				.build();
	}

	public static ArkivUtsendingsInfo arkivUtsendingsInfoWithNavNoVarslingOldVarselStructure(String varseltekst, String varselKontaktInfo) {
		return ArkivUtsendingsInfo.builder()
				.navNoVarsling(new ArkivNavNoVarsling(varselKontaktInfo, varseltekst))
				.build();
	}

	public static ArkivUtsendingsInfo arkivUtsendingsInfoWithDigitalPostadresseOldVarselStructure() {
		return ArkivUtsendingsInfo.builder()
				.digitalPostadresse(new ArkivDigitalPostadresse(DIGITALPOSTKASSE_ADRESSE))
				.build();
	}

	public static ArkivUtsendingsInfo arkivUtsendingsInfoWithNavNoVarsling(String epostadresse, String tittel, String epostTekst, String mobilnummer, String smstekst, LocalDateTime varslingstidspunkt) {
		return ArkivUtsendingsInfo.builder()
				.navNoVarsling(new ArkivNavNoVarsling(null, null))
				.epostVarsel(epostadresse == null ? emptyList() :
						List.of(ArkivEpostVarsel.builder()
								.epostadresse(epostadresse)
								.tittel(tittel)
								.tekst(epostTekst)
								.varslingstidspunkt(varslingstidspunkt)
								.build()))
				.smsVarsel(mobilnummer == null ? emptyList() :
						List.of(ArkivSmsVarsel.builder()
								.mobilnummer(mobilnummer)
								.tekst(smstekst)
								.varslingstidspunkt(varslingstidspunkt)
								.build()))
				.build();
	}

	public static ArkivUtsendingsInfo arkivUtsendingsInfoWithDigitalPostadresse(String epostadresse, String tittel, String epostTekst, String mobilnummer, String smstekst, LocalDateTime varslingstidspunkt) {
		return ArkivUtsendingsInfo.builder()
				.digitalPostadresse(new ArkivDigitalPostadresse(DIGITALPOSTKASSE_ADRESSE))
				.epostVarsel(epostadresse == null ? emptyList() :
						List.of(ArkivEpostVarsel.builder()
								.epostadresse(epostadresse)
								.tittel(tittel)
								.tekst(epostTekst)
								.varslingstidspunkt(varslingstidspunkt)
								.build()))
				.smsVarsel(mobilnummer == null ? emptyList() :
						List.of(ArkivSmsVarsel.builder()
								.mobilnummer(mobilnummer)
								.tekst(smstekst)
								.varslingstidspunkt(varslingstidspunkt)
								.build()))
				.build();
	}
}
