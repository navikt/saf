package no.nav.saf.anticorruptionlayer.joark.domain;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.AvsenderMottakerIdTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.DokumentStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.SkjermingTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.UtsendingsKanalCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.DokumentInfoDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.LogiskVedleggDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.SaksrelasjonDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.TilleggsopplysningDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.UtsendingsInfoDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.VariantDto;
import no.nav.saf.domain.visningsmodell.AvsenderMottakerIdType;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class JournalpostDtoTestObjects {
	static final String DOKUMENT_INFO_ID = "1234";
	static final VariantFormatCode VARIANT_FORMAT_CODE_ARKIV = VariantFormatCode.ARKIV;
	static final VariantFormatCode VARIANT_FORMAT_CODE_SLADDET = VariantFormatCode.SLADDET;
	static final SkjermingTypeCode SKJERMING_TYPE_CODE_POL = SkjermingTypeCode.POL;
	static final String BREVKODE = "brevkodeX";
	static final long JOURNALPOST_ID = 417457822L;
	static final String INNHOLD = "MASKERT_FELT";
	static final Date DATO_OPPRETTET = new Date(1000L);
	static final Date AVS_RETUR_DATO = new Date(2000L);
	static final Date SENDT_PRINT_DATO = new Date(3000L);
	static final Date EKSPEDERT_DATO = new Date(4000L);
	static final Date DOKUMENT_DATO = new Date(5000L);
	static final Date JOURNAL_DATO = new Date(6000L);
	static final Date MOTTAT_DATO = new Date(7000L);
	static final Date DATO_FERDIGSTILT = new Date(8000L);
	static final Date LEST_DATO = new Date(9000L);
	static final String FNR = "12345678901";
	static final String AKTOER_ID = "4321098765431";
	static final String SAKS_ID = "12345";
	static final String ORG_NR = "54321";
	static final String ARKIVSAK_NR = "1337";
	static final FagsystemCode FAKSYSTEM_CODE = FagsystemCode.FS22;
	static final AvsenderMottakerIdTypeCode AVSENDER_MOTTAKER_ID_TYPE_CODE = AvsenderMottakerIdTypeCode.FNR;
	static final FagomradeCode FAGOMRADE = FagomradeCode.STO;
	static final String JOURNALFOERT_AV = "Automatisk jobb";
	static final String BEHANDLINGSTEMA = "ab0072";
	static final String BEHANDLINGSTEMANAVN = "Foreldrepenger ved adopsjon";
	static final String AVSENDER_MOTTAKER_NAVN = "Bjarne Betjent";
	static final String AVSENDER_MOTTAKER_LAND = "NO";
	static final String JOURNALFOERENDE_ENHET = "2990";
	static final String OPPRETTET_AV_NAVN = "Max Mekker";
	static final String TILLEGGSOPPLYSNING_NOKKEL = "bucid";
	static final String TILLEGGSOPPLYSNING_VERDI = "21521";
	static final String FILNAVN_1 = "filnavn1";
	static final String FILNAVN_2 = "filnavn2";
	static final String AVSENDER_MOTTAKER_ID = "00000000000";
	static final AvsenderMottakerIdType AVSENDER_MOTTAKER_ID_TYPE = AvsenderMottakerIdType.FNR;
	static final String LOGISK_VEDLEGG_ID = "logisk1";
	static final String LOGISK_VEDLEGG_TITTEL = "logisktittel";
	static final String DOKUMENTTYPE_ID = "00000001";
	static final String FILUUID_1 = "abcd";
	static final String FILUUID_2 = "dcba";
	static final String BRUKER_ID_PERSON = "11111111111";
	static final String BRUKER_ID_ORGANISASJON = "999999999";
	static final String ANTALL_RETUR = "3";
	static final String KANAL_REFERANSE_ID = "KANAL REFERANSE ID";
	static final String FILTYPE_1 = "PDFA";
	static final String FILTYPE_2 = "PDF";
	public static final String ADRESSELINJE1 = "adresselinje1";
	public static final String ADRESSELINJE2 = "adresselinje2";
	public static final String ADRESSELINJE3 = "adresselinje3";
	public static final String POSTNUMMER = "postnummer";
	public static final String POSTSTED = "poststed";
	public static final String LANDKODE = "landkode";
	public static final String ADRESSETEKST_KONVOLUTT = """
			adresselinje1
			adresselinje2
			postnummer poststed
			landkode""";
	public static final String DIGITALKONTAKT_INFORMASJON = "{\n          \"epost\": \"epostaddress3@nav.no\",\n          \"sms\": \"11111111\"\n        }";
	public static final String VARSEL_TITTEL1 = "Vedtak fra NAV,";
	public static final String VARSEL_TEKST1 = "<!DOCTYPE html><html><head><title>Vedtak fra NAV</title></head><body><!DOCTYPE html>\n" +
			"<html>\n" +
			"\t<head>\n" +
			"\t\t<title>Vedtak fra NAV</title>\n" +
			"\t</head>\n" +
			"\t<body>\n" +
			"\t\t<p>Hei!</p>\n" +
			"\t\t<p>Du har fått et vedtak fra NAV.</p>\n" +
			"\t\t<p>Logg inn på nav.no for å lese det.</p>\n" +
			"\t\t<p>Vennlig hilsen</p>\n" +
			"\t\t<p>NAV</p>\n" +
			"\t</body>\n" +
			"</html></body></html>";
	public static final String VARSEL_MELDING1 = "{\"epost\":\"Tittel Vedtak fra NAV, Tekst <!DOCTYPE html><html><head>" +
			"<title>Vedtak fra NAV</title></head><body><!DOCTYPE html>\\n<html>\\n\\t<head>\\n\\t\\t<title>Vedtak fra NAV</title>\\n\\t</head>\\n\\t<body>\\n\\t\\t<p>Hei!</p>\\n\\t\\t<p>Du har fått et vedtak fra NAV.</p>\\n\\t\\t<p>Logg inn på nav.no for å lese det.</p>\\n\\t\\t<p>Vennlig hilsen</p>\\n\\t\\t<p>NAV</p>\\n\\t</body>\\n</html></body></html>\\n\",\"sms\":null}";

	public static final String VARSEL_MELDING2 = "{\"epost\":\"Tittel Melding fra NAV, Tekst <!DOCTYPE html><html><head><title>Melding fra NAV</title></head><body><!DOCTYPE html>\\n<html>\\n    <head>\\n        <title>Melding fra NAV</title>\\n    </head>\\n    <body>\\n        <p>Hei!</p>\\n        <p>Du har fått en melding fra NAV.</p>\\n        <p>Logg inn på nav.no for å lese den.</p>\\n        <p>Vennlig hilsen</p>\\n        <p>NAV</p>\\n    </body>\\n</html></body></html>\\n\",\"sms\":null}";
	public static final String VARSEL_TITTEL2 = "Melding fra NAV,";
	public static final String VARSEL_TEKST2 = "<!DOCTYPE html><html><head><title>Melding fra NAV</title></head><body><!DOCTYPE html>\n" +
			"<html>\n" +
			"    <head>\n" +
			"        <title>Melding fra NAV</title>\n" +
			"    </head>\n" +
			"    <body>\n" +
			"        <p>Hei!</p>\n" +
			"        <p>Du har fått en melding fra NAV.</p>\n" +
			"        <p>Logg inn på nav.no for å lese den.</p>\n" +
			"        <p>Vennlig hilsen</p>\n" +
			"        <p>NAV</p>\n" +
			"    </body>\n" +
			"</html></body></html>";
	public static final String VARSEL_MELDING3 = "{\"epost\":null,\"sms\":\"Hei! Du har fått en melding fra NAV. Logg inn på nav.no for å lese den. Vennlig hilsen NAV\"}";
	public static final String VARSEL_KONTAKTINFO_3 = "{\"epost\":null,\"sms\":\"12345678\"}";
	public static final String PHONENUMMER = "12345678";
	public static final String SMS_VARSELTEKST = "Hei! Du har fått en melding fra NAV. Logg inn på nav.no for å lese den. Vennlig hilsen NAV";
	public static final String DIGITALPOSTKASSEADRESSE = "tom.tom#2541";

	public static final String VARSEL_MELDING5 = "{\"epost\":\"Tittel Vedtak fra NAV, Tekst <!DOCTYPE html><html><head>" +
			"<title>Vedtak fra NAV</title></head><body><!DOCTYPE html>\\n<html>\\n\\t<head>\\n\\t\\t<title>Vedtak fra NAV</title>\\n\\t</head>\\n\\t<body>\\n\\t\\t<p>Hei!</p>\\n\\t\\t<p>Du har fått et vedtak fra NAV.</p>\\n\\t\\t<p>Logg inn på nav.no for å lese det.</p>\\n\\t\\t<p>Vennlig hilsen</p>\\n\\t\\t<p>NAV</p>\\n\\t</body>\\n</html></body></html>\\n\",\"sms\":\"Hei! Du har fått en melding fra NAV. Logg inn på nav.no for å lese den. Vennlig hilsen NAV\"}";
	public static final String VARSEL_KONTAKTINFO_5 = "{\"epost\":\"epostaddress3@nav.no\",\"sms\":\"12345678\"}";
	public static final String VARSEL_MELDING4 = "{\"epost\":null,\"sms\":null}";

	static JournalpostDto buildJournalpostDtoUtgaaendeType(JournalStatusCode journalStatusCode, UtsendingsInfoDto utsendingsInfoDto, UtsendingsKanalCode kanalCode) {
		return baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.U)
				.saksrelasjon(new SaksrelasjonDto(SAKS_ID, false, FAKSYSTEM_CODE, null, null,
						null, null, null, null, null))
				.journalstatus(journalStatusCode)
				.journalDato(JOURNAL_DATO)
				.dokumentDato(DOKUMENT_DATO)
				.lestDato(LEST_DATO)
				.avsReturDato(AVS_RETUR_DATO)
				.sendtPrintDato(SENDT_PRINT_DATO)
				.ekspedertDato(EKSPEDERT_DATO)
				.antallRetur(ANTALL_RETUR)
				.utsendingskanal(kanalCode)
				.utsendingsInfo(utsendingsInfoDto)
				.build();
	}

	static JournalpostDto buildJournalpostDtoInngaaendeType() {
		return baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.I)
				.saksrelasjon(new SaksrelasjonDto(SAKS_ID, false, FAKSYSTEM_CODE, null, null,
						null, null, null, null, null))
				.mottattDato(MOTTAT_DATO)
				.journalDato(JOURNAL_DATO)
				.build();
	}

	static JournalpostDto buildJournalpostDtoInternNotatType() {
		return baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.N)
				.saksrelasjon(new SaksrelasjonDto(SAKS_ID, false, FAKSYSTEM_CODE, null, null,
						null, null, null, null, null))
				.journalDato(JOURNAL_DATO)
				.build();
	}

	static JournalpostDto buildJournalpostDtoPenSaksrelasjonDto() {
		return baseJournalpostDto()
				.journalposttype(JournalpostTypeCode.N)
				.saksrelasjon(new SaksrelasjonDto(SAKS_ID, false, FagsystemCode.PEN, null, null,
						null, null, null, null, null))
				.build();
	}

	static JournalpostDto.JournalpostDtoBuilder baseJournalpostDto() {
		return JournalpostDto.builder()
				.journalpostId(JOURNALPOST_ID)
				.nextJournalpostId(405252858L)
				.innhold(INNHOLD)
				.fagomrade(FAGOMRADE)
				.behandlingstema(BEHANDLINGSTEMA)
				.behandlingstemanavn(BEHANDLINGSTEMANAVN)
				.avsenderMottakerId(AVSENDER_MOTTAKER_ID)
				.avsenderMottakerNavn(AVSENDER_MOTTAKER_NAVN)
				.avsenderMottakerLand(AVSENDER_MOTTAKER_LAND)
				.journalforendeEnhet(JOURNALFOERENDE_ENHET)
				.journalfortAvNavn(JOURNALFOERT_AV)
				.opprettetAvNavn(OPPRETTET_AV_NAVN)
				.datoOpprettet(DATO_OPPRETTET)
				.journalstatus(JournalStatusCode.J)
				.skjerming(SKJERMING_TYPE_CODE_POL)
				.kanalReferanseId(KANAL_REFERANSE_ID)
				.tilleggsopplysninger(Collections.singletonList(TilleggsopplysningDto.builder()
						.nokkel(TILLEGGSOPPLYSNING_NOKKEL)
						.verdi(TILLEGGSOPPLYSNING_VERDI)
						.build()))
				.dokumenter(buildDokumenter());
	}


	private static List<DokumentInfoDto> buildDokumenter() {
		return Collections.singletonList(
				DokumentInfoDto.builder()
						.dokumentInfoId(JournalpostDtoTestObjects.DOKUMENT_INFO_ID)
						.tittel("veldigViktigTittel")
						.brevkode(JournalpostDtoTestObjects.BREVKODE)
						.dokumenttypeId(JournalpostDtoTestObjects.DOKUMENTTYPE_ID)
						.dokumentstatus(DokumentStatusCode.FERDIGSTILT)
						.datoFerdigstilt(JournalpostDtoTestObjects.DATO_FERDIGSTILT)
						.origJournalpostId(JournalpostDtoTestObjects.JOURNALPOST_ID)
						.skjerming(JournalpostDtoTestObjects.SKJERMING_TYPE_CODE_POL)
						.logiske(logiskeVedlegg())
						.varianter(Arrays.asList(VariantDto.builder()
										.skjerming(JournalpostDtoTestObjects.SKJERMING_TYPE_CODE_POL)
										.variantf(JournalpostDtoTestObjects.VARIANT_FORMAT_CODE_ARKIV)
										.filnavn(JournalpostDtoTestObjects.FILNAVN_1)
										.filuuid(JournalpostDtoTestObjects.FILUUID_1)
										.filtype(JournalpostDtoTestObjects.FILTYPE_1)
										.build(),
								VariantDto.builder()
										.skjerming(null)
										.variantf(JournalpostDtoTestObjects.VARIANT_FORMAT_CODE_SLADDET)
										.filnavn(JournalpostDtoTestObjects.FILNAVN_2)
										.filuuid(JournalpostDtoTestObjects.FILUUID_2)
										.filtype(JournalpostDtoTestObjects.FILTYPE_2)
										.build()))
						.build());
	}

	public static UtsendingsInfoDto createFysiskPostadresseDto() {
		return UtsendingsInfoDto.builder()
				.fysiskPostadresse(UtsendingsInfoDto.FysiskPostadresseDto.builder()
						.adresselinje1(ADRESSELINJE1)
						.adresselinje2(ADRESSELINJE2)
						.postnummer(POSTNUMMER)
						.poststed(POSTSTED)
						.landkode(LANDKODE)
						.build())
				.build();
	}

	public static UtsendingsInfoDto createDitttNavVarsel(String varseltekst, String varselKontaktInfo) {
		return UtsendingsInfoDto.builder()
				.navNoVarsling(UtsendingsInfoDto.NavNoVarslingDto.builder()
						.varselSendtTil(varselKontaktInfo)
						.varseltekst(varseltekst)
						.build())
				.build();
	}

	public static UtsendingsInfoDto createDigitalPostadresse() {
		return UtsendingsInfoDto.builder()
				.digitalPostadresse(UtsendingsInfoDto.DigitalPostadresseDto.builder()
						.digitalpostkasseAdresse(DIGITALPOSTKASSEADRESSE)
						.build())
				.build();
	}

	private static List<LogiskVedleggDto> logiskeVedlegg() {
		LogiskVedleggDto logiskVedleggDto = new LogiskVedleggDto();
		logiskVedleggDto.setVedleggId(JournalpostDtoTestObjects.LOGISK_VEDLEGG_ID);
		logiskVedleggDto.setTittel(JournalpostDtoTestObjects.LOGISK_VEDLEGG_TITTEL);
		return Collections.singletonList(logiskVedleggDto);
	}
}
