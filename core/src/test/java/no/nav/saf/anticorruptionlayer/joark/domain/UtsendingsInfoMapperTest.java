package no.nav.saf.anticorruptionlayer.joark.domain;

import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.UtsendingsInfoDto;
import no.nav.saf.domain.visningsmodell.Utsendingsinfo;
import org.junit.jupiter.api.Test;

import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.ADRESSELINJE1;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.ADRESSELINJE2;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.ADRESSELINJE3;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.EPOST_MELDING_MED_KOMMA_FORVENTET_ADRESSE;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.EPOST_MELDING_MED_KOMMA_FORVENTET_TITTEL;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.EPOST_MELDING_MED_KOMMA_FORVENTET_VARSLINGSTEKST;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.EPOST_MELDING_MED_KOMMA_INPUT_DIGITAL_KONTAKTINFO;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.EPOST_MELDING_MED_KOMMA_INPUT_VARSLINGSTEKST;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.EPOST_VEDTAK_FORVENTET_ADRESSE;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.EPOST_VEDTAK_FORVENTET_TITTEL;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.EPOST_VEDTAK_FORVENTET_VARSLINGSTEKST;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.EPOST_VEDTAK_INPUT_DIGITAL_KONTAKTINFO;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.EPOST_VEDTAK_INPUT_VARSLINGSTEKST;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.FORVENTET_ADRESSETEKST_KONVOLUTT;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.INGEN_POSTNUMMER_POSTSTED_FORVENTET_ADRESSETEKST_KONVOLUTT;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.KUN_POSTNUMMER_POSTSTED_LANDKODE_FORVENTET_ADRESSETEKST_KONVOLUTT;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.LANDKODE;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.POSTNUMMER;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.POSTSTED;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.SMS_OG_EPOST_AVVIK_NULL_INPUT_DIGITAL_KONTAKTINFO;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.SMS_OG_EPOST_AVVIK_NULL_INPUT_VARSLINGSTEKST;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.SMS_OG_EPOST_VEDTAK_INPUT_ADRESSE;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.SMS_OG_EPOST_VEDTAK_INPUT_VARSLINGSTEKST;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.SMS_VEDTAK_FORVENTET_ADRESSE;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.SMS_VEDTAK_FORVENTET_VARSLINGSTEKST;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.SMS_VEDTAK_INPUT_DIGITAL_KONTAKTINFO;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.SMS_VEDTAK_INPUT_VARSLINGSTEKST;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.createUtsendingsInfoDtoWithNavNoVarsling;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.UtsendingsKanalCode.NAV_NO;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.UtsendingsKanalCode.S;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UtsendingsInfoMapperTest {

	private final UtsendingsInfoMapper mapper = new UtsendingsInfoMapper();

	@Test
	void shouldMapSmsAndEpostVedtakVarsel() {
		Utsendingsinfo utsendingsinfo = mapper.mapUtsendingsInfo(createUtsendingsInfoDtoWithNavNoVarsling(SMS_OG_EPOST_VEDTAK_INPUT_VARSLINGSTEKST, SMS_OG_EPOST_VEDTAK_INPUT_ADRESSE), NAV_NO)
				.orElse(null);

		assertThat(utsendingsinfo).isNotNull();
		assertThat(utsendingsinfo.getFysiskpostSendt()).isNull();
		assertThat(utsendingsinfo.getDigitalpostSendt()).isNull();
		Utsendingsinfo.EpostVarselSendt epostVarselSendt = utsendingsinfo.getEpostVarselSendt();
		assertThat(epostVarselSendt.getAdresse()).isEqualTo(EPOST_VEDTAK_FORVENTET_ADRESSE);
		assertThat(epostVarselSendt.getTittel()).isEqualTo(EPOST_VEDTAK_FORVENTET_TITTEL);
		assertThat(epostVarselSendt.getVarslingstekst()).isEqualToIgnoringWhitespace(EPOST_VEDTAK_FORVENTET_VARSLINGSTEKST);
		Utsendingsinfo.SmsVarselSendt smsVarselSendt = utsendingsinfo.getSmsVarselSendt();
		assertThat(smsVarselSendt.getAdresse()).isEqualTo(SMS_VEDTAK_FORVENTET_ADRESSE);
		assertThat(smsVarselSendt.getVarslingstekst()).isEqualTo(SMS_VEDTAK_FORVENTET_VARSLINGSTEKST);
	}

	@Test
	void shouldMapEpostVedtak() {
		Utsendingsinfo utsendingsinfo = mapper.mapUtsendingsInfo(createUtsendingsInfoDtoWithNavNoVarsling(EPOST_VEDTAK_INPUT_VARSLINGSTEKST, EPOST_VEDTAK_INPUT_DIGITAL_KONTAKTINFO), NAV_NO)
				.orElse(null);

		assertThat(utsendingsinfo).isNotNull();
		assertThat(utsendingsinfo.getFysiskpostSendt()).isNull();
		assertThat(utsendingsinfo.getDigitalpostSendt()).isNull();
		Utsendingsinfo.EpostVarselSendt epostVarselSendt = utsendingsinfo.getEpostVarselSendt();
		assertThat(epostVarselSendt.getAdresse()).isEqualTo(EPOST_VEDTAK_FORVENTET_ADRESSE);
		assertThat(epostVarselSendt.getTittel()).isEqualTo(EPOST_VEDTAK_FORVENTET_TITTEL);
		assertThat(epostVarselSendt.getVarslingstekst()).isEqualToIgnoringWhitespace(EPOST_VEDTAK_FORVENTET_VARSLINGSTEKST);
	}

	@Test
	void shouldMapEpostMeldingMedKommaITittelOgVarslingstekst() {
		Utsendingsinfo utsendingsinfo = mapper.mapUtsendingsInfo(createUtsendingsInfoDtoWithNavNoVarsling(EPOST_MELDING_MED_KOMMA_INPUT_VARSLINGSTEKST, EPOST_MELDING_MED_KOMMA_INPUT_DIGITAL_KONTAKTINFO), NAV_NO)
				.orElse(null);

		assertThat(utsendingsinfo).isNotNull();
		Utsendingsinfo.EpostVarselSendt epostVarselSendt = utsendingsinfo.getEpostVarselSendt();
		assertThat(epostVarselSendt.getAdresse()).isEqualTo(EPOST_MELDING_MED_KOMMA_FORVENTET_ADRESSE);
		assertThat(epostVarselSendt.getTittel()).isEqualTo(EPOST_MELDING_MED_KOMMA_FORVENTET_TITTEL);
		assertThat(epostVarselSendt.getVarslingstekst()).isEqualToIgnoringWhitespace(EPOST_MELDING_MED_KOMMA_FORVENTET_VARSLINGSTEKST);
		assertNull(utsendingsinfo.getSmsVarselSendt());
		assertNull(utsendingsinfo.getDigitalpostSendt());
		assertNull(utsendingsinfo.getFysiskpostSendt());
	}

	@Test
	void shouldMapKunEpostVarsel() {
		Utsendingsinfo utsendingsinfo = mapper.mapUtsendingsInfo(createUtsendingsInfoDtoWithNavNoVarsling(EPOST_VEDTAK_INPUT_VARSLINGSTEKST, EPOST_VEDTAK_INPUT_DIGITAL_KONTAKTINFO), NAV_NO)
				.orElse(null);

		assertThat(utsendingsinfo).isNotNull();
		Utsendingsinfo.EpostVarselSendt epostVarselSendt = utsendingsinfo.getEpostVarselSendt();
		assertThat(epostVarselSendt.getAdresse()).isEqualTo(EPOST_VEDTAK_FORVENTET_ADRESSE);
		assertThat(epostVarselSendt.getTittel()).isEqualTo(EPOST_VEDTAK_FORVENTET_TITTEL);
		assertThat(epostVarselSendt.getVarslingstekst()).isEqualToIgnoringWhitespace(EPOST_VEDTAK_FORVENTET_VARSLINGSTEKST);
		assertNull(utsendingsinfo.getSmsVarselSendt());
		assertNull(utsendingsinfo.getDigitalpostSendt());
		assertNull(utsendingsinfo.getFysiskpostSendt());
	}

	@Test
	void shouldMapKunSmsVarsel() {
		Utsendingsinfo utsendingsinfo = mapper.mapUtsendingsInfo(createUtsendingsInfoDtoWithNavNoVarsling(SMS_VEDTAK_INPUT_VARSLINGSTEKST, SMS_VEDTAK_INPUT_DIGITAL_KONTAKTINFO), NAV_NO)
				.orElse(null);

		assertThat(utsendingsinfo).isNotNull();
		Utsendingsinfo.SmsVarselSendt smsVarselSendt = utsendingsinfo.getSmsVarselSendt();
		assertThat(smsVarselSendt.getAdresse()).isEqualTo(SMS_VEDTAK_FORVENTET_ADRESSE);
		assertThat(smsVarselSendt.getVarslingstekst()).isEqualTo(SMS_VEDTAK_FORVENTET_VARSLINGSTEKST);
		assertNull(utsendingsinfo.getEpostVarselSendt());
		assertNull(utsendingsinfo.getDigitalpostSendt());
		assertNull(utsendingsinfo.getFysiskpostSendt());
	}

	@Test
	void shouldMapNullNavVarselSendtWhenInputHasAvvik() {
		Utsendingsinfo utsendingsinfo = mapper.mapUtsendingsInfo(createUtsendingsInfoDtoWithNavNoVarsling(SMS_OG_EPOST_AVVIK_NULL_INPUT_VARSLINGSTEKST, SMS_OG_EPOST_AVVIK_NULL_INPUT_DIGITAL_KONTAKTINFO), NAV_NO)
				.orElse(null);

		assertThat(utsendingsinfo).isNull();
	}

	@Test
	void shouldMapFysiskPostadresse() {
		UtsendingsInfoDto utsendingsInfoDto = UtsendingsInfoDto.builder()
				.fysiskPostadresse(UtsendingsInfoDto.FysiskPostadresseDto.builder()
						.adresselinje1(ADRESSELINJE1)
						.adresselinje2(ADRESSELINJE2)
						.adresselinje3(ADRESSELINJE3)
						.postnummer(POSTNUMMER)
						.poststed(POSTSTED)
						.landkode(LANDKODE)
						.build())
				.build();

		Utsendingsinfo utsendingsinfo = mapper.mapUtsendingsInfo(utsendingsInfoDto, S)
				.orElse(null);

		assertThat(utsendingsinfo).isNotNull();
		Utsendingsinfo.FysiskpostSendt fysiskpostSendt = utsendingsinfo.getFysiskpostSendt();
		assertThat(fysiskpostSendt.getAdressetekstKonvolutt()).isEqualTo(FORVENTET_ADRESSETEKST_KONVOLUTT);
		assertThat(utsendingsinfo.getEpostVarselSendt()).isNull();
		assertThat(utsendingsinfo.getSmsVarselSendt()).isNull();
		assertThat(utsendingsinfo.getDigitalpostSendt()).isNull();
	}

	@Test
	void shouldOnlyConcatenateAdresselinjeWhenPostnummerOgPoststedIsNull() {
		UtsendingsInfoDto utsendingsInfoDto = UtsendingsInfoDto.builder()
				.fysiskPostadresse(UtsendingsInfoDto.FysiskPostadresseDto.builder()
						.adresselinje1(ADRESSELINJE1)
						.adresselinje2(ADRESSELINJE2)
						.adresselinje3(ADRESSELINJE3)
						.postnummer(null)
						.poststed(null)
						.landkode(LANDKODE)
						.build())
				.build();

		Utsendingsinfo utsendingsinfo = mapper.mapUtsendingsInfo(utsendingsInfoDto, S)
				.orElse(null);

		assertThat(utsendingsinfo).isNotNull();
		Utsendingsinfo.FysiskpostSendt fysiskpostSendt = utsendingsinfo.getFysiskpostSendt();
		assertThat(fysiskpostSendt.getAdressetekstKonvolutt()).isEqualTo(INGEN_POSTNUMMER_POSTSTED_FORVENTET_ADRESSETEKST_KONVOLUTT);
		assertThat(utsendingsinfo.getEpostVarselSendt()).isNull();
		assertThat(utsendingsinfo.getSmsVarselSendt()).isNull();
		assertThat(utsendingsinfo.getDigitalpostSendt()).isNull();
	}

	@Test
	void shouldOnlyConcatenatePostnummerPoststedWhenOtherFieldsAreNull() {
		UtsendingsInfoDto utsendingsInfoDto = UtsendingsInfoDto.builder()
				.fysiskPostadresse(UtsendingsInfoDto.FysiskPostadresseDto.builder()
						.adresselinje1(null)
						.adresselinje2(null)
						.adresselinje3(null)
						.postnummer(POSTNUMMER)
						.poststed(POSTSTED)
						.landkode(LANDKODE)
						.build())
				.build();

		Utsendingsinfo utsendingsinfo = mapper.mapUtsendingsInfo(utsendingsInfoDto, S)
				.orElse(null);

		assertThat(utsendingsinfo).isNotNull();
		Utsendingsinfo.FysiskpostSendt fysiskpostSendt = utsendingsinfo.getFysiskpostSendt();
		assertThat(fysiskpostSendt.getAdressetekstKonvolutt()).isEqualTo(KUN_POSTNUMMER_POSTSTED_LANDKODE_FORVENTET_ADRESSETEKST_KONVOLUTT);
		assertThat(utsendingsinfo.getEpostVarselSendt()).isNull();
		assertThat(utsendingsinfo.getSmsVarselSendt()).isNull();
		assertThat(utsendingsinfo.getDigitalpostSendt()).isNull();
	}

	@Test
	void shouldReturnUtsendingsInfoNullWhenNavNoVarselTekstOgInfoIsNull() {
		Utsendingsinfo utsendingsinfo = mapper.mapUtsendingsInfo(createUtsendingsInfoDtoWithNavNoVarsling(null, null), NAV_NO)
				.orElse(null);

		assertThat(utsendingsinfo).isNull();
	}

	@Test
	void shouldMapSmsVedtak() {
		Utsendingsinfo utsendingsinfo = mapper.mapUtsendingsInfo(createUtsendingsInfoDtoWithNavNoVarsling(SMS_VEDTAK_INPUT_VARSLINGSTEKST, SMS_VEDTAK_INPUT_DIGITAL_KONTAKTINFO), NAV_NO).orElse(null);

		assertThat(utsendingsinfo).isNotNull();
		Utsendingsinfo.SmsVarselSendt smsVarselSendt = utsendingsinfo.getSmsVarselSendt();
		assertThat(smsVarselSendt.getAdresse()).isEqualTo(SMS_VEDTAK_FORVENTET_ADRESSE);
		assertThat(smsVarselSendt.getVarslingstekst()).isEqualToIgnoringWhitespace(SMS_VEDTAK_FORVENTET_VARSLINGSTEKST);
		assertThat(utsendingsinfo.getEpostVarselSendt()).isNull();
		assertThat(utsendingsinfo.getFysiskpostSendt()).isNull();
		assertThat(utsendingsinfo.getDigitalpostSendt()).isNull();
	}
}
