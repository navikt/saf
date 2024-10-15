package no.nav.saf.anticorruptionlayer.joark.domain;

import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.UtsendingsInfoDto;
import no.nav.saf.domain.visningsmodell.Utsendingsinfo;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

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
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.FORVENTET_NORSK_ADRESSETEKST_KONVOLUTT;
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
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.VARSLINGSTIDSPUNKT;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.createUtsendingsInfoDtoWithDigitalPostadresse;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.createUtsendingsInfoDtoWithNavNoVarsling;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoDtoTestObjects.createUtsendingsInfoDtoWithNavNoVarslingOldVarselStructure;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoMapper.NORGE_LANDKODE;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.UtsendingsKanalCode.NAV_NO;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.UtsendingsKanalCode.S;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.UtsendingsKanalCode.SDP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UtsendingsInfoMapperTest {

	private static final String SMS_TYPE = "SMS";
	private static final String EPOST_TYPE = "EPOST";

	@Test
	void shouldMapSmsAndEpostVedtakVarselFromOldFormat() {
		Optional<Utsendingsinfo> utsendingsinfoOptional = UtsendingsInfoMapper.mapUtsendingsInfo(createUtsendingsInfoDtoWithNavNoVarslingOldVarselStructure(SMS_OG_EPOST_VEDTAK_INPUT_VARSLINGSTEKST, SMS_OG_EPOST_VEDTAK_INPUT_ADRESSE), NAV_NO);

		assertThat(utsendingsinfoOptional).isPresent();

		utsendingsinfoOptional.ifPresent(utsendingsinfo -> {
			assertThat(utsendingsinfo.getFysiskpostSendt()).isNull();
			assertThat(utsendingsinfo.getDigitalpostSendt()).isNull();

			List<Utsendingsinfo.VarselSendt> varselSendt = utsendingsinfo.getVarselSendt();
			assertThat(varselSendt).hasSize(2);
			assertSmsVarselGeneratedNavNo(false, utsendingsinfo);
			assertEpostVarselGeneratedNavNo(false, utsendingsinfo);
		});
	}

	@Test
	void shouldMapSmsAndEpostVedtakVarsel() {
		Optional<Utsendingsinfo> utsendingsinfoOptional = UtsendingsInfoMapper.mapUtsendingsInfo(createUtsendingsInfoDtoWithNavNoVarsling(EPOST_VEDTAK_FORVENTET_ADRESSE, EPOST_VEDTAK_FORVENTET_TITTEL, EPOST_VEDTAK_FORVENTET_VARSLINGSTEKST, SMS_VEDTAK_FORVENTET_ADRESSE, SMS_VEDTAK_FORVENTET_VARSLINGSTEKST, VARSLINGSTIDSPUNKT), NAV_NO);

		assertThat(utsendingsinfoOptional).isPresent();

		utsendingsinfoOptional.ifPresent(utsendingsinfo -> {
			assertThat(utsendingsinfo.getFysiskpostSendt()).isNull();
			assertThat(utsendingsinfo.getDigitalpostSendt()).isNull();

			List<Utsendingsinfo.VarselSendt> varselSendt = utsendingsinfo.getVarselSendt();
			assertThat(varselSendt).hasSize(2);

			assertEpostVarselGeneratedNavNo(true, utsendingsinfo);
			assertSmsVarselGeneratedNavNo(true, utsendingsinfo);
		});
	}

	@Test
	void shouldMapSmsAndEpostVedtakVarselSDP() {
		Optional<Utsendingsinfo> utsendingsinfoOptional = UtsendingsInfoMapper.mapUtsendingsInfo(createUtsendingsInfoDtoWithDigitalPostadresse(EPOST_VEDTAK_FORVENTET_ADRESSE, EPOST_VEDTAK_FORVENTET_TITTEL, EPOST_VEDTAK_FORVENTET_VARSLINGSTEKST, SMS_VEDTAK_FORVENTET_ADRESSE, SMS_VEDTAK_FORVENTET_VARSLINGSTEKST, VARSLINGSTIDSPUNKT), SDP);

		assertThat(utsendingsinfoOptional).isPresent();

		utsendingsinfoOptional.ifPresent(utsendingsinfo -> {
			assertThat(utsendingsinfo.getFysiskpostSendt()).isNull();
			assertThat(utsendingsinfo.getDigitalpostSendt()).isNotNull();

			List<Utsendingsinfo.VarselSendt> varselSendt = utsendingsinfo.getVarselSendt();
			assertThat(varselSendt).hasSize(2);

			assertEpostVarselGenerated(true, utsendingsinfo);
			assertSmsVarselGenerated(true, utsendingsinfo);
		});
	}

	@Test
	void shouldMapEpostVedtak() {
		Utsendingsinfo utsendingsinfo = UtsendingsInfoMapper.mapUtsendingsInfo(createUtsendingsInfoDtoWithNavNoVarslingOldVarselStructure(EPOST_VEDTAK_INPUT_VARSLINGSTEKST, EPOST_VEDTAK_INPUT_DIGITAL_KONTAKTINFO), NAV_NO)
				.orElse(null);

		assertThat(utsendingsinfo).isNotNull();
		assertThat(utsendingsinfo.getFysiskpostSendt()).isNull();
		assertThat(utsendingsinfo.getDigitalpostSendt()).isNull();

		List<Utsendingsinfo.VarselSendt> varselSendt = utsendingsinfo.getVarselSendt();
		assertThat(varselSendt).hasSize(1);

		assertEpostVarselGeneratedNavNo(false, utsendingsinfo);

		assertThat(varselSendt).noneSatisfy(element -> {
			assertThat(element.getType()).isEqualTo(SMS_TYPE);
		});
	}

	@Test
	void shouldMapEpostMeldingMedKommaITittelOgVarslingstekst() {
		Utsendingsinfo utsendingsinfo = UtsendingsInfoMapper.mapUtsendingsInfo(createUtsendingsInfoDtoWithNavNoVarslingOldVarselStructure(EPOST_MELDING_MED_KOMMA_INPUT_VARSLINGSTEKST, EPOST_MELDING_MED_KOMMA_INPUT_DIGITAL_KONTAKTINFO), NAV_NO)
				.orElse(null);

		assertThat(utsendingsinfo).isNotNull();
		Utsendingsinfo.EpostVarselSendt epostVarselSendt = utsendingsinfo.getEpostVarselSendt();
		assertThat(epostVarselSendt.getAdresse()).isEqualTo(EPOST_MELDING_MED_KOMMA_FORVENTET_ADRESSE);
		assertThat(epostVarselSendt.getTittel()).isEqualTo(EPOST_MELDING_MED_KOMMA_FORVENTET_TITTEL);
		assertThat(epostVarselSendt.getVarslingstekst()).isEqualToIgnoringWhitespace(EPOST_MELDING_MED_KOMMA_FORVENTET_VARSLINGSTEKST);
		assertNull(utsendingsinfo.getSmsVarselSendt());
		assertNull(utsendingsinfo.getDigitalpostSendt());
		assertNull(utsendingsinfo.getFysiskpostSendt());

		List<Utsendingsinfo.VarselSendt> varselSendt = utsendingsinfo.getVarselSendt();
		assertThat(varselSendt).hasSize(1);
		assertThat(varselSendt).anySatisfy(element -> {
			assertThat(element.getType()).isEqualTo(EPOST_TYPE);
			assertThat(element.getVarslingstekst()).isEqualTo(EPOST_MELDING_MED_KOMMA_FORVENTET_VARSLINGSTEKST);
			assertThat(element.getAdresse()).isEqualTo(EPOST_MELDING_MED_KOMMA_FORVENTET_ADRESSE);
			assertThat(element.getTittel()).isEqualTo(EPOST_MELDING_MED_KOMMA_FORVENTET_TITTEL);
			assertThat(element.getVarslingstidspunkt()).isNull();
		});
		assertThat(varselSendt).noneSatisfy(element -> {
			assertThat(element.getType()).isEqualTo(SMS_TYPE);
		});
	}

	@Test
	void shouldMapKunEpostVarsel() {
		Utsendingsinfo utsendingsinfo = UtsendingsInfoMapper.mapUtsendingsInfo(createUtsendingsInfoDtoWithNavNoVarslingOldVarselStructure(EPOST_VEDTAK_INPUT_VARSLINGSTEKST, EPOST_VEDTAK_INPUT_DIGITAL_KONTAKTINFO), NAV_NO)
				.orElse(null);

		assertThat(utsendingsinfo).isNotNull();
		assertNull(utsendingsinfo.getSmsVarselSendt());
		assertNull(utsendingsinfo.getDigitalpostSendt());
		assertNull(utsendingsinfo.getFysiskpostSendt());

		List<Utsendingsinfo.VarselSendt> varselSendt = utsendingsinfo.getVarselSendt();
		assertThat(varselSendt).hasSize(1);

		assertEpostVarselGeneratedNavNo(false, utsendingsinfo);

		assertThat(varselSendt).noneSatisfy(element -> {
			assertThat(element.getType()).isEqualTo(SMS_TYPE);
		});
	}

	@Test
	void shouldMapKunSmsVarsel() {
		Utsendingsinfo utsendingsinfo = UtsendingsInfoMapper.mapUtsendingsInfo(createUtsendingsInfoDtoWithNavNoVarslingOldVarselStructure(SMS_VEDTAK_INPUT_VARSLINGSTEKST, SMS_VEDTAK_INPUT_DIGITAL_KONTAKTINFO), NAV_NO)
				.orElse(null);

		assertThat(utsendingsinfo).isNotNull();
		assertNull(utsendingsinfo.getEpostVarselSendt());
		assertNull(utsendingsinfo.getDigitalpostSendt());
		assertNull(utsendingsinfo.getFysiskpostSendt());

		List<Utsendingsinfo.VarselSendt> varselSendt = utsendingsinfo.getVarselSendt();
		assertThat(varselSendt).hasSize(1);

		assertSmsVarselGeneratedNavNo(false, utsendingsinfo);

		assertThat(varselSendt).noneSatisfy(element -> assertThat(element.getType()).isEqualTo(EPOST_TYPE));
	}

	@Test
	void shouldMapNullNavVarselSendtWhenInputHasAvvik() {
		Optional<Utsendingsinfo> utsendingsinfo = UtsendingsInfoMapper.mapUtsendingsInfo(createUtsendingsInfoDtoWithNavNoVarslingOldVarselStructure(SMS_OG_EPOST_AVVIK_NULL_INPUT_VARSLINGSTEKST, SMS_OG_EPOST_AVVIK_NULL_INPUT_DIGITAL_KONTAKTINFO), NAV_NO);

		assertThat(utsendingsinfo).isEmpty();
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

		Utsendingsinfo utsendingsinfo = UtsendingsInfoMapper.mapUtsendingsInfo(utsendingsInfoDto, S)
				.orElse(null);

		assertThat(utsendingsinfo).isNotNull();
		Utsendingsinfo.FysiskpostSendt fysiskpostSendt = utsendingsinfo.getFysiskpostSendt();
		assertThat(fysiskpostSendt.getAdressetekstKonvolutt()).isEqualTo(FORVENTET_ADRESSETEKST_KONVOLUTT);
		assertThat(utsendingsinfo.getEpostVarselSendt()).isNull();
		assertThat(utsendingsinfo.getSmsVarselSendt()).isNull();
		assertThat(utsendingsinfo.getDigitalpostSendt()).isNull();
	}

	@Test
	void shouldConcatenateNorskAdresseUtenLandKode() {
		UtsendingsInfoDto utsendingsInfoDto = UtsendingsInfoDto.builder()
				.fysiskPostadresse(UtsendingsInfoDto.FysiskPostadresseDto.builder()
						.adresselinje1(ADRESSELINJE1)
						.adresselinje2(ADRESSELINJE2)
						.postnummer(POSTNUMMER)
						.poststed(POSTSTED)
						.landkode(NORGE_LANDKODE)
						.build())
				.build();

		Utsendingsinfo utsendingsinfo = UtsendingsInfoMapper.mapUtsendingsInfo(utsendingsInfoDto, S)
				.orElse(null);

		assertThat(utsendingsinfo).isNotNull();
		Utsendingsinfo.FysiskpostSendt fysiskpostSendt = utsendingsinfo.getFysiskpostSendt();
		assertThat(fysiskpostSendt.getAdressetekstKonvolutt()).isEqualTo(FORVENTET_NORSK_ADRESSETEKST_KONVOLUTT);
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

		Utsendingsinfo utsendingsinfo = UtsendingsInfoMapper.mapUtsendingsInfo(utsendingsInfoDto, S)
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

		Utsendingsinfo utsendingsinfo = UtsendingsInfoMapper.mapUtsendingsInfo(utsendingsInfoDto, S)
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
		Optional<Utsendingsinfo> utsendingsinfo = UtsendingsInfoMapper.mapUtsendingsInfo(createUtsendingsInfoDtoWithNavNoVarslingOldVarselStructure(null, null), NAV_NO);

		assertThat(utsendingsinfo).isEmpty();
	}

	@Test
	void shouldMapSmsVedtakOldStructure() {
		Utsendingsinfo utsendingsinfo = UtsendingsInfoMapper.mapUtsendingsInfo(createUtsendingsInfoDtoWithNavNoVarslingOldVarselStructure(SMS_VEDTAK_INPUT_VARSLINGSTEKST, SMS_VEDTAK_INPUT_DIGITAL_KONTAKTINFO), NAV_NO).orElse(null);

		assertThat(utsendingsinfo).isNotNull();

		assertThat(utsendingsinfo.getEpostVarselSendt()).isNull();
		assertThat(utsendingsinfo.getFysiskpostSendt()).isNull();
		assertThat(utsendingsinfo.getDigitalpostSendt()).isNull();

		List<Utsendingsinfo.VarselSendt> varselSendt = utsendingsinfo.getVarselSendt();
		assertThat(varselSendt).hasSize(1);

		assertSmsVarselGeneratedNavNo(false, utsendingsinfo);

		assertThat(varselSendt).noneSatisfy(element -> {
			assertThat(element.getType()).isEqualTo(EPOST_TYPE);
		});
	}

	@Test
	void shouldMapOnlySmsVedtak() {
		Optional<Utsendingsinfo> utsendingsinfoOptional = UtsendingsInfoMapper.mapUtsendingsInfo(createUtsendingsInfoDtoWithNavNoVarsling(null, null, null, SMS_VEDTAK_FORVENTET_ADRESSE, SMS_VEDTAK_FORVENTET_VARSLINGSTEKST, VARSLINGSTIDSPUNKT), NAV_NO);

		assertThat(utsendingsinfoOptional).isPresent();

		utsendingsinfoOptional.ifPresent(utsendingsinfo -> {
			assertThat(utsendingsinfo.getEpostVarselSendt()).isNull();
			assertThat(utsendingsinfo.getFysiskpostSendt()).isNull();
			assertThat(utsendingsinfo.getDigitalpostSendt()).isNull();

			List<Utsendingsinfo.VarselSendt> varselSendt = utsendingsinfo.getVarselSendt();
			assertThat(varselSendt).hasSize(1);

			assertSmsVarselGeneratedNavNo(true, utsendingsinfo);

			assertThat(varselSendt).noneSatisfy(element -> assertThat(element.getType()).isEqualTo(EPOST_TYPE));
		});
	}


	private static void assertEpostVarselGeneratedNavNo(boolean withVarslingstidspunkt, Utsendingsinfo utsendingsinfo) {
		Utsendingsinfo.EpostVarselSendt epostVarselSendt = utsendingsinfo.getEpostVarselSendt();
		assertThat(epostVarselSendt.getAdresse()).isEqualTo(EPOST_VEDTAK_FORVENTET_ADRESSE);
		assertThat(epostVarselSendt.getTittel()).isEqualTo(EPOST_VEDTAK_FORVENTET_TITTEL);
		assertThat(epostVarselSendt.getVarslingstekst()).isEqualToIgnoringWhitespace(EPOST_VEDTAK_FORVENTET_VARSLINGSTEKST);

		assertEpostVarselGenerated(withVarslingstidspunkt, utsendingsinfo);
	}

	private static void assertSmsVarselGeneratedNavNo(boolean withVarslingstidspunkt, Utsendingsinfo utsendingsinfo) {
		Utsendingsinfo.SmsVarselSendt smsVarselSendt = utsendingsinfo.getSmsVarselSendt();
		assertThat(smsVarselSendt.getAdresse()).isEqualTo(SMS_VEDTAK_FORVENTET_ADRESSE);
		assertThat(smsVarselSendt.getVarslingstekst()).isEqualTo(SMS_VEDTAK_FORVENTET_VARSLINGSTEKST);

		assertSmsVarselGenerated(withVarslingstidspunkt, utsendingsinfo);
	}

	private static void assertEpostVarselGenerated(boolean withVarslingstidspunkt, Utsendingsinfo utsendingsinfo) {
		List<Utsendingsinfo.VarselSendt> varselSendt = utsendingsinfo.getVarselSendt();
		assertThat(varselSendt).anySatisfy(element -> {
			assertThat(element.getType()).isEqualTo(EPOST_TYPE);
			assertThat(element.getVarslingstekst()).isEqualTo(EPOST_VEDTAK_FORVENTET_VARSLINGSTEKST);
			assertThat(element.getAdresse()).isEqualTo(EPOST_VEDTAK_FORVENTET_ADRESSE);
			assertThat(element.getTittel()).isEqualTo(EPOST_VEDTAK_FORVENTET_TITTEL);
			if (withVarslingstidspunkt) {
				assertThat(element.getVarslingstidspunkt()).isCloseTo(VARSLINGSTIDSPUNKT, within(1, ChronoUnit.SECONDS));
			} else {
				assertThat(element.getVarslingstidspunkt()).isNull();
			}
		});
	}

	private static void assertSmsVarselGenerated(boolean withVarslingstidspunkt, Utsendingsinfo utsendingsinfo) {
		List<Utsendingsinfo.VarselSendt> varselSendt = utsendingsinfo.getVarselSendt();
		assertThat(varselSendt).anySatisfy(element -> {
			assertThat(element.getType()).isEqualTo(SMS_TYPE);
			assertThat(element.getVarslingstekst()).isEqualTo(SMS_VEDTAK_FORVENTET_VARSLINGSTEKST);
			assertThat(element.getAdresse()).isEqualTo(SMS_VEDTAK_FORVENTET_ADRESSE);
			if (withVarslingstidspunkt) {
				assertThat(element.getVarslingstidspunkt()).isCloseTo(VARSLINGSTIDSPUNKT, within(1, ChronoUnit.SECONDS));
			} else {
				assertThat(element.getVarslingstidspunkt()).isNull();
			}
		});
	}
}
