package no.nav.saf.anticorruptionlayer.joark.domain;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.UtsendingsInfoDto;
import no.nav.saf.domain.visningsmodell.Utsendingsinfo;
import org.junit.jupiter.api.Test;

import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.ADRESSELINJE1;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.ADRESSELINJE2;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.ADRESSELINJE3;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.DIGITALKONTAKT_INFORMASJON;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.PHONENUMMER;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.SMS_VARSELTEKST;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.VARSEL_KONTAKTINFO_3;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.VARSEL_KONTAKTINFO_5;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.VARSEL_MELDING1;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.VARSEL_MELDING2;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.VARSEL_MELDING3;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.VARSEL_MELDING4;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.VARSEL_MELDING5;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.VARSEL_TEKST1;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.VARSEL_TEKST2;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.VARSEL_TITTEL1;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.VARSEL_TITTEL2;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.buildJournalpostDtoUtgaaendeType;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.createDitttNavVarsel;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode.E;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.UtsendingsKanalCode.NAV_NO;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.UtsendingsKanalCode.S;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class UtsendingsInfoMapperTest {

	private final UtsendingsInfoMapper mapper = new UtsendingsInfoMapper();

	@Test
	void shouldMapSmsAndEpostVarsel() {
		Utsendingsinfo utsendingsinfo = mapper.mapUtsendingsInfo(createDitttNavVarsel(VARSEL_MELDING5, VARSEL_KONTAKTINFO_5), NAV_NO)
				.orElse(null);


		assertNull(utsendingsinfo.getFysiskpostSendt());
		assertNull(utsendingsinfo.getDigitalpostSendt());
		assertEquals(VARSEL_TITTEL1, utsendingsinfo.getEpostVarselSendt().getTittel());
		assertEquals(VARSEL_TEKST1.strip(), utsendingsinfo.getEpostVarselSendt().getVarslingstekst());
		assertEquals("epostaddress3@nav.no", utsendingsinfo.getEpostVarselSendt().getAdresse());
		assertEquals(SMS_VARSELTEKST, utsendingsinfo.getSmsVarselSendt().getVarslingstekst());
		assertEquals(PHONENUMMER, utsendingsinfo.getSmsVarselSendt().getAdresse());
	}

	@Test
	void shouldMapUtsendingsKanalErNAV_NO() {
		Utsendingsinfo utsendingsinfo = mapper.mapUtsendingsInfo(createDitttNavVarsel(VARSEL_MELDING1, DIGITALKONTAKT_INFORMASJON), NAV_NO)
				.orElse(null);


		assertNull(utsendingsinfo.getFysiskpostSendt());
		assertNull(utsendingsinfo.getDigitalpostSendt());
		assertEquals(VARSEL_TITTEL1, utsendingsinfo.getEpostVarselSendt().getTittel());
		assertEquals(VARSEL_TEKST1.strip(), utsendingsinfo.getEpostVarselSendt().getVarslingstekst());
		assertEquals("epostaddress3@nav.no", utsendingsinfo.getEpostVarselSendt().getAdresse());
	}

	@Test
	void shouldMapEpostVarsel() {
		Utsendingsinfo utsendingsinfo = mapper.mapUtsendingsInfo(createDitttNavVarsel(VARSEL_MELDING2, DIGITALKONTAKT_INFORMASJON), NAV_NO)
				.orElse(null);

		assertEquals(VARSEL_TITTEL2, utsendingsinfo.getEpostVarselSendt().getTittel());
		assertEquals(VARSEL_TEKST2.strip(), utsendingsinfo.getEpostVarselSendt().getVarslingstekst());
		assertNull(utsendingsinfo.getSmsVarselSendt());
		assertNull(utsendingsinfo.getDigitalpostSendt());
		assertNull(utsendingsinfo.getFysiskpostSendt());
	}

	@Test
	void shouldMapNullNavVarselSendt() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(E, createDitttNavVarsel(VARSEL_MELDING4, VARSEL_MELDING4), NAV_NO);
		journalpostDto.setFagomrade(FagomradeCode.OKO);

		Utsendingsinfo utsendingsinfo = mapper.mapUtsendingsInfo(journalpostDto.getUtsendingsInfo(), NAV_NO)
				.orElse(null);

		assertNull(utsendingsinfo);
	}

	@Test
	void shouldOnlyConcatenateAdresselinjeWhenPostnummerOgPoststedIsNull() {
		UtsendingsInfoDto utsendingsInfoDto = UtsendingsInfoDto.builder()
				.fysiskPostadresse(UtsendingsInfoDto.FysiskPostadresseDto.builder()
						.adresselinje1(ADRESSELINJE1)
						.adresselinje2(ADRESSELINJE2)
						.adresselinje3(ADRESSELINJE3)
						.poststed(null)
						.poststed(null)
						.build())
				.build();

		Utsendingsinfo utsendingsinfo = mapper.mapUtsendingsInfo(utsendingsInfoDto, S)
				.orElse(null);

		assertEquals(ADRESSELINJE1 + "\n" + ADRESSELINJE2 + "\n" + ADRESSELINJE3, utsendingsinfo.getFysiskpostSendt().getAdressetekstKonvolutt());
		assertNull(utsendingsinfo.getDigitalpostSendt());
	}

	@Test
	void shouldReturnUtsendingsInfoNullWhenNavNoVarselTekstOgInfoIsNull() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(E, createDitttNavVarsel(null, null), NAV_NO);
		journalpostDto.setFagomrade(FagomradeCode.OKO);

		Utsendingsinfo utsendingsinfo = mapper.mapUtsendingsInfo(createDitttNavVarsel(null, null), NAV_NO)
				.orElse(null);

		assertNull(utsendingsinfo);
	}

	@Test
	void shouldMapSmsTekstOgKontaktInfo() {
		JournalpostDto journalpostDto = buildJournalpostDtoUtgaaendeType(E, createDitttNavVarsel(VARSEL_MELDING3, VARSEL_KONTAKTINFO_3), NAV_NO);
		journalpostDto.setFagomrade(FagomradeCode.OKO);

		Utsendingsinfo utsendingsinfo = mapper.mapUtsendingsInfo(createDitttNavVarsel(VARSEL_MELDING3, VARSEL_KONTAKTINFO_3), NAV_NO).orElse(null);

		assertEquals(SMS_VARSELTEKST, utsendingsinfo.getSmsVarselSendt().getVarslingstekst());
		assertEquals(PHONENUMMER, utsendingsinfo.getSmsVarselSendt().getAdresse());
		assertNull(utsendingsinfo.getEpostVarselSendt());
		assertNull(utsendingsinfo.getDigitalpostSendt());
	}
}
