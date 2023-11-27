package no.nav.saf.anticorruptionlayer.joark;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivAvsenderMottaker;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivBruker;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivJournalpost;
import no.nav.saf.domain.visningsmodell.AvsenderMottaker;
import no.nav.saf.domain.visningsmodell.AvsenderMottakerIdType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static no.nav.saf.anticorruptionlayer.joark.ArkivAvsenderMottakerMapper.mapArkivAvsenderMottaker;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.AVSENDER_MOTTAKER_ID;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.AVSENDER_MOTTAKER_ID_TYPE_CODE;
import static no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostTestObjects.baseArkivJournalpost;
import static org.assertj.core.api.Assertions.assertThat;

class ArkivAvsenderMottakerMapperTest {

	@Test
	void shouldReturnErLikBrukerTrueWhenBrukerIdIsSameAsAvsenderMottakerId() {
		ArkivJournalpost arkivJournalpost = baseArkivJournalpost()
				.type(JournalpostTypeCode.I.name())
				.avsenderMottaker(new ArkivAvsenderMottaker(AVSENDER_MOTTAKER_ID, null, null, null))
				.bruker(new ArkivBruker(AVSENDER_MOTTAKER_ID, null))
				.build();

		AvsenderMottaker avsenderMottaker = mapArkivAvsenderMottaker(arkivJournalpost);

		assertThat(avsenderMottaker.isErLikBruker()).isTrue();
	}

	@Test
	void shouldMapAvsenderMottakerIdTypeNull() {
		ArkivJournalpost arkivJournalpost = baseArkivJournalpost()
				.avsenderMottaker(new ArkivAvsenderMottaker(null, null, null, null))
				.build();

		AvsenderMottaker avsenderMottaker = mapArkivAvsenderMottaker(arkivJournalpost);

		assertThat(avsenderMottaker.getType()).isEqualTo(AvsenderMottakerIdType.NULL);
	}

	@Test
	void shouldMapAvsenderMottakerIdTypeFNR() {
		ArkivJournalpost arkivJournalpost = baseArkivJournalpost()
				.avsenderMottaker(new ArkivAvsenderMottaker(AVSENDER_MOTTAKER_ID, AVSENDER_MOTTAKER_ID_TYPE_CODE.name(), null, null))
				.build();

		AvsenderMottaker avsenderMottaker = mapArkivAvsenderMottaker(arkivJournalpost);

		assertThat(avsenderMottaker.getType()).isEqualTo(AvsenderMottakerIdType.FNR);
	}

	@Test
	void shouldMapAvsenderMottakerIdTypeTest() {
		ArkivJournalpost arkivJournalpost = baseArkivJournalpost()
				.avsenderMottaker(new ArkivAvsenderMottaker(AVSENDER_MOTTAKER_ID, "TEST", null, null))
				.build();

		AvsenderMottaker avsenderMottaker = mapArkivAvsenderMottaker(arkivJournalpost);

		assertThat(avsenderMottaker.getType()).isEqualTo(AvsenderMottakerIdType.UKJENT);
	}

	@Test
	void shouldMapAvsenderMottakerEmptyObjectWhenInputIsNull() {
		ArkivJournalpost arkivJournalpost = baseArkivJournalpost()
				.avsenderMottaker(null)
				.build();

		AvsenderMottaker avsenderMottaker = mapArkivAvsenderMottaker(arkivJournalpost);

		assertThat(avsenderMottaker).hasAllNullFieldsOrPropertiesExcept("type", "erLikBruker")
				.isNotNull();
		assertThat(avsenderMottaker.getType()).isEqualTo(AvsenderMottakerIdType.NULL);
	}

	@Nested
	@DisplayName("Test mapping når AvsenderMottakerIdType ikke er satt")
	class AvsenderMottakerIdTypeIsNull {

		@Test
		void shouldMapAvsenderMottakerIdTypeNullWhenAvsenderMottakerIdIsNull() {
			ArkivJournalpost arkivJournalpost = baseArkivJournalpost()
					.avsenderMottaker(new ArkivAvsenderMottaker(null, null, null, null))
					.build();

			AvsenderMottaker avsenderMottaker = mapArkivAvsenderMottaker(arkivJournalpost);

			assertThat(avsenderMottaker.getType()).isEqualTo(AvsenderMottakerIdType.NULL);
		}

		@Test
		void shouldMapAvsenderMottakerIdTypeORGNRWhenAvsenderMottakerIdIsOfLength9() {
			ArkivJournalpost arkivJournalpost = baseArkivJournalpost()
					.avsenderMottaker(new ArkivAvsenderMottaker("123456789", null, null, null))
					.build();

			AvsenderMottaker avsenderMottaker = mapArkivAvsenderMottaker(arkivJournalpost);

			assertThat(avsenderMottaker.getType()).isEqualTo(AvsenderMottakerIdType.ORGNR);
		}

		@ParameterizedTest
		@ValueSource(strings = {"00000000000", "10000000000", "20000000000", "30000000000",
				"40000000000", "50000000000", "60000000000", "70000000000"})
		void shouldMapAvsenderMottakerIdTypeFNRWhenAvsenderMottakerIdIs11DigitsLongAnd1DigitInRange0To7(String input) {
			ArkivJournalpost arkivJournalpost = baseArkivJournalpost()
					.avsenderMottaker(new ArkivAvsenderMottaker(input, null, null, null))
					.build();

			AvsenderMottaker avsenderMottaker = mapArkivAvsenderMottaker(arkivJournalpost);

			assertThat(avsenderMottaker.getType()).isEqualTo(AvsenderMottakerIdType.FNR);
		}

		@ParameterizedTest
		@ValueSource(strings = {"80000000000", "90000000000"})
		@DisplayName("Test mapping av TSS-id")
		void shouldMapAvsenderMottakerIdTypeUKJENTWhenAvsenderMottakerIdIs11DigitsLongAndFirstDigitIs8Or9(String input) {
			ArkivJournalpost arkivJournalpost = baseArkivJournalpost()
					.avsenderMottaker(new ArkivAvsenderMottaker(input, null, null, null))
					.build();

			AvsenderMottaker avsenderMottaker = mapArkivAvsenderMottaker(arkivJournalpost);

			assertThat(avsenderMottaker.getType()).isEqualTo(AvsenderMottakerIdType.UKJENT);
		}

		@ParameterizedTest
		@ValueSource(strings = {"EE:70000000"})
		@DisplayName("Test mapping av referanse til estiske trygdemyndigheter.")
		void shouldMapAvsenderMottakerIdTypeUKJENTWhenAvsenderMottakerIdIsLength11AndNonNumeric(String input) {
			ArkivJournalpost arkivJournalpost = baseArkivJournalpost()
					.avsenderMottaker(new ArkivAvsenderMottaker(input, null, null, null))
					.build();

			AvsenderMottaker avsenderMottaker = mapArkivAvsenderMottaker(arkivJournalpost);

			assertThat(avsenderMottaker.getType()).isEqualTo(AvsenderMottakerIdType.UKJENT);
		}

		@ParameterizedTest
		@ValueSource(strings = {"12345", "1234567890123", ""})
		void shouldMapAvsenderMottakerIdTypeUKJENT(String input) {
			ArkivJournalpost arkivJournalpost = baseArkivJournalpost()
					.avsenderMottaker(new ArkivAvsenderMottaker(input, null, null, null))
					.build();

			AvsenderMottaker avsenderMottaker = mapArkivAvsenderMottaker(arkivJournalpost);

			assertThat(avsenderMottaker.getType()).isEqualTo(AvsenderMottakerIdType.UKJENT);
		}
	}
}