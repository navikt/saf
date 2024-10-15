package no.nav.saf.anticorruptionlayer.joark;

import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivFysiskPostadresse;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivUtsendingsInfo;
import no.nav.saf.domain.visningsmodell.Utsendingsinfo;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoMapper.mapArkivUtsendingsInfo;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.ADRESSELINJE1;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.ADRESSELINJE2;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.ADRESSELINJE3;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.EPOST_MELDING_MED_KOMMA_FORVENTET_ADRESSE;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.EPOST_MELDING_MED_KOMMA_FORVENTET_TITTEL;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.EPOST_MELDING_MED_KOMMA_FORVENTET_VARSLINGSTEKST;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.EPOST_MELDING_MED_KOMMA_INPUT_DIGITAL_KONTAKTINFO;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.EPOST_MELDING_MED_KOMMA_INPUT_VARSLINGSTEKST;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.EPOST_VEDTAK_FORVENTET_ADRESSE;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.EPOST_VEDTAK_FORVENTET_TITTEL;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.EPOST_VEDTAK_FORVENTET_VARSLINGSTEKST;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.EPOST_VEDTAK_INPUT_DIGITAL_KONTAKTINFO;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.EPOST_VEDTAK_INPUT_VARSLINGSTEKST;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.FORVENTET_ADRESSETEKST_KONVOLUTT;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.FORVENTET_ADRESSETEKST_KONVOLUTT_UTEN_LANDKODE_FOR_NORSKADRESSE;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.INGEN_POSTNUMMER_POSTSTED_FORVENTET_ADRESSETEKST_KONVOLUTT;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.KUN_POSTNUMMER_POSTSTED_LANDKODE_FORVENTET_ADRESSETEKST_KONVOLUTT;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.LANDKODE;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.POSTNUMMER;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.POSTSTED;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.SMS_OG_EPOST_AVVIK_NULL_INPUT_DIGITAL_KONTAKTINFO;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.SMS_OG_EPOST_AVVIK_NULL_INPUT_VARSLINGSTEKST;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.SMS_OG_EPOST_VEDTAK_INPUT_ADRESSE;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.SMS_OG_EPOST_VEDTAK_INPUT_VARSLINGSTEKST;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.SMS_VEDTAK_FORVENTET_ADRESSE;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.SMS_VEDTAK_FORVENTET_VARSLINGSTEKST;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.SMS_VEDTAK_INPUT_DIGITAL_KONTAKTINFO;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.SMS_VEDTAK_INPUT_VARSLINGSTEKST;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.VARSLINGSTIDSPUNKT;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.arkivUtsendingsInfoWithDigitalPostadresse;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.arkivUtsendingsInfoWithNavNoVarsling;
import static no.nav.saf.anticorruptionlayer.joark.ArkivUtsendingsInfoTestObjects.arkivUtsendingsInfoWithNavNoVarslingOldVarselStructure;
import static no.nav.saf.anticorruptionlayer.joark.domain.UtsendingsInfoMapper.NORGE_LANDKODE;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.UtsendingsKanalCode.NAV_NO;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.UtsendingsKanalCode.S;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.UtsendingsKanalCode.SDP;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ArkivUtsendingsInfoMapperTest {

	private static final String SMS_TYPE = "SMS";
	private static final String EPOST_TYPE = "EPOST";

	@Test
	void shouldMapSmsAndEpostVedtakVarselFromOldFormat() {
		Optional<Utsendingsinfo> utsendingsinfoOptional = mapArkivUtsendingsInfo(arkivUtsendingsInfoWithNavNoVarslingOldVarselStructure(SMS_OG_EPOST_VEDTAK_INPUT_VARSLINGSTEKST, SMS_OG_EPOST_VEDTAK_INPUT_ADRESSE), NAV_NO);

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
		Optional<Utsendingsinfo> utsendingsinfoOptional = mapArkivUtsendingsInfo(arkivUtsendingsInfoWithNavNoVarsling(EPOST_VEDTAK_FORVENTET_ADRESSE, EPOST_VEDTAK_FORVENTET_TITTEL, EPOST_VEDTAK_FORVENTET_VARSLINGSTEKST, SMS_VEDTAK_FORVENTET_ADRESSE, SMS_VEDTAK_FORVENTET_VARSLINGSTEKST, VARSLINGSTIDSPUNKT), NAV_NO);

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
		Optional<Utsendingsinfo> utsendingsinfoOptional = mapArkivUtsendingsInfo(arkivUtsendingsInfoWithDigitalPostadresse(EPOST_VEDTAK_FORVENTET_ADRESSE, EPOST_VEDTAK_FORVENTET_TITTEL, EPOST_VEDTAK_FORVENTET_VARSLINGSTEKST, SMS_VEDTAK_FORVENTET_ADRESSE, SMS_VEDTAK_FORVENTET_VARSLINGSTEKST, VARSLINGSTIDSPUNKT), SDP);

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
		Utsendingsinfo utsendingsinfo = mapArkivUtsendingsInfo(arkivUtsendingsInfoWithNavNoVarslingOldVarselStructure(EPOST_VEDTAK_INPUT_VARSLINGSTEKST, EPOST_VEDTAK_INPUT_DIGITAL_KONTAKTINFO), NAV_NO)
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
		Utsendingsinfo utsendingsinfo = mapArkivUtsendingsInfo(arkivUtsendingsInfoWithNavNoVarslingOldVarselStructure(EPOST_MELDING_MED_KOMMA_INPUT_VARSLINGSTEKST, EPOST_MELDING_MED_KOMMA_INPUT_DIGITAL_KONTAKTINFO), NAV_NO)
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
		Utsendingsinfo utsendingsinfo = mapArkivUtsendingsInfo(arkivUtsendingsInfoWithNavNoVarslingOldVarselStructure(EPOST_VEDTAK_INPUT_VARSLINGSTEKST, EPOST_VEDTAK_INPUT_DIGITAL_KONTAKTINFO), NAV_NO)
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
		Utsendingsinfo utsendingsinfo = mapArkivUtsendingsInfo(arkivUtsendingsInfoWithNavNoVarslingOldVarselStructure(SMS_VEDTAK_INPUT_VARSLINGSTEKST, SMS_VEDTAK_INPUT_DIGITAL_KONTAKTINFO), NAV_NO)
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
		Optional<Utsendingsinfo> utsendingsinfo = mapArkivUtsendingsInfo(arkivUtsendingsInfoWithNavNoVarslingOldVarselStructure(SMS_OG_EPOST_AVVIK_NULL_INPUT_VARSLINGSTEKST, SMS_OG_EPOST_AVVIK_NULL_INPUT_DIGITAL_KONTAKTINFO), NAV_NO);

		assertThat(utsendingsinfo).isEmpty();
	}

	@Test
	void shouldMapNorskFysiskPostadresse() {
		ArkivUtsendingsInfo arkivUtsendingsInfo = ArkivUtsendingsInfo.builder()
				.fysiskPostadresse(ArkivFysiskPostadresse.builder()
						.adresselinje1(ADRESSELINJE1)
						.adresselinje2(ADRESSELINJE2)
						.postnummer(POSTNUMMER)
						.poststed(POSTSTED)
						.landkode(NORGE_LANDKODE)
						.build())
				.build();

		Utsendingsinfo utsendingsinfo = mapArkivUtsendingsInfo(arkivUtsendingsInfo, S)
				.orElse(null);

		assertThat(utsendingsinfo).isNotNull();
		Utsendingsinfo.FysiskpostSendt fysiskpostSendt = utsendingsinfo.getFysiskpostSendt();
		assertThat(fysiskpostSendt.getAdressetekstKonvolutt()).isEqualTo(FORVENTET_ADRESSETEKST_KONVOLUTT_UTEN_LANDKODE_FOR_NORSKADRESSE);
		assertThat(utsendingsinfo.getEpostVarselSendt()).isNull();
		assertThat(utsendingsinfo.getSmsVarselSendt()).isNull();
		assertThat(utsendingsinfo.getDigitalpostSendt()).isNull();
	}

	@Test
	void shouldMapFysiskPostadresse() {
		ArkivUtsendingsInfo arkivUtsendingsInfo = ArkivUtsendingsInfo.builder()
				.fysiskPostadresse(ArkivFysiskPostadresse.builder()
						.adresselinje1(ADRESSELINJE1)
						.adresselinje2(ADRESSELINJE2)
						.adresselinje3(ADRESSELINJE3)
						.postnummer(POSTNUMMER)
						.poststed(POSTSTED)
						.landkode(LANDKODE)
						.build())
				.build();

		Utsendingsinfo utsendingsinfo = mapArkivUtsendingsInfo(arkivUtsendingsInfo, S)
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
		ArkivUtsendingsInfo arkivUtsendingsInfo = ArkivUtsendingsInfo.builder()
				.fysiskPostadresse(ArkivFysiskPostadresse.builder()
						.adresselinje1(ADRESSELINJE1)
						.adresselinje2(ADRESSELINJE2)
						.adresselinje3(ADRESSELINJE3)
						.postnummer(null)
						.poststed(null)
						.landkode(LANDKODE)
						.build())
				.build();

		Utsendingsinfo utsendingsinfo = mapArkivUtsendingsInfo(arkivUtsendingsInfo, S)
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
		ArkivUtsendingsInfo arkivUtsendingsInfo = ArkivUtsendingsInfo.builder()
				.fysiskPostadresse(ArkivFysiskPostadresse.builder()
						.adresselinje1(null)
						.adresselinje2(null)
						.adresselinje3(null)
						.postnummer(POSTNUMMER)
						.poststed(POSTSTED)
						.landkode(LANDKODE)
						.build())
				.build();

		Utsendingsinfo utsendingsinfo = mapArkivUtsendingsInfo(arkivUtsendingsInfo, S)
				.orElse(null);

		assertThat(utsendingsinfo).isNotNull();
		Utsendingsinfo.FysiskpostSendt fysiskpostSendt = utsendingsinfo.getFysiskpostSendt();
		assertThat(fysiskpostSendt.getAdressetekstKonvolutt()).isEqualTo(KUN_POSTNUMMER_POSTSTED_LANDKODE_FORVENTET_ADRESSETEKST_KONVOLUTT);
		assertThat(utsendingsinfo.getEpostVarselSendt()).isNull();
		assertThat(utsendingsinfo.getSmsVarselSendt()).isNull();
		assertThat(utsendingsinfo.getDigitalpostSendt()).isNull();
	}

	@Test
	void shouldReturnUtsendingsinfoNullWhenNavNoVarselTekstOgInfoIsNull() {
		Optional<Utsendingsinfo> utsendingsinfo = mapArkivUtsendingsInfo(arkivUtsendingsInfoWithNavNoVarslingOldVarselStructure(null, null), NAV_NO);

		assertThat(utsendingsinfo).isEmpty();
	}

	@Test
	void shouldMapSmsVedtakOldStructure() {
		Utsendingsinfo utsendingsinfo = mapArkivUtsendingsInfo(arkivUtsendingsInfoWithNavNoVarslingOldVarselStructure(SMS_VEDTAK_INPUT_VARSLINGSTEKST, SMS_VEDTAK_INPUT_DIGITAL_KONTAKTINFO), NAV_NO).orElse(null);

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
		Optional<Utsendingsinfo> utsendingsinfoOptional = mapArkivUtsendingsInfo(arkivUtsendingsInfoWithNavNoVarsling(null, null, null, SMS_VEDTAK_FORVENTET_ADRESSE, SMS_VEDTAK_FORVENTET_VARSLINGSTEKST, VARSLINGSTIDSPUNKT), NAV_NO);

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
