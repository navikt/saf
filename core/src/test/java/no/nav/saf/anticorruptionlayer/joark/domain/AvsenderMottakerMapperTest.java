package no.nav.saf.anticorruptionlayer.joark.domain;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.BrukerDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.domain.visningsmodell.AvsenderMottaker;
import no.nav.saf.domain.visningsmodell.AvsenderMottakerIdType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.AVSENDER_MOTTAKER_ID;
import static no.nav.saf.anticorruptionlayer.joark.domain.JournalpostDtoTestObjects.baseJournalpostDto;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class AvsenderMottakerMapperTest {
    private final AvsenderMottakerMapper mapper = new AvsenderMottakerMapper();

    @Test
    void shouldAvsenderMottakerErLikBrukerTrueWhenBrukerIsSameAsAvsenderMottakerId() {
        JournalpostDto journalpostDto = baseJournalpostDto()
                .journalposttype(JournalpostTypeCode.I)
                .avsenderMottakerId(AVSENDER_MOTTAKER_ID)
                .bruker(BrukerDto.builder().brukerId(AVSENDER_MOTTAKER_ID).build())
                .build();

        AvsenderMottaker avsenderMottaker = mapper.map(journalpostDto);

        assertTrue(avsenderMottaker.isErLikBruker());
    }


    @Test
    void shouldMapAvsenderMottakerIdTypeNull() {
        JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInngaaendeType();
        journalpostDto.setAvsenderMottakerId(null);

        AvsenderMottaker avsenderMottaker = mapper.map(journalpostDto);

        assertThat(avsenderMottaker.getType(), is(AvsenderMottakerIdType.NULL));
    }

    @Test
    void shouldMapAvsenderMottakerIdTypeFNR() {
        JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInngaaendeType();
        journalpostDto.setAvsenderMottakerIdType(JournalpostDtoTestObjects.AVSENDER_MOTTAKER_ID_TYPE_CODE);

        AvsenderMottaker avsenderMottaker = mapper.map(journalpostDto);

        assertThat(avsenderMottaker.getType(), is(AvsenderMottakerIdType.FNR));
    }

    @Nested
    @DisplayName("Test mapping når AvsenderMottakerIdType ikke er satt")
    class AvsenderMottakerIdTypeIsNull {

        @Test
        void shouldMapAvsenderMottakerIdTypeNullWhenAvsenderMottakerIdIsNull() {
            JournalpostDto journalpostDto = buildJournalpostDtoAvsenderMottakerIdTypeNull();
            journalpostDto.setAvsenderMottakerId(null);

            AvsenderMottaker avsenderMottaker = mapper.map(journalpostDto);

            assertThat(avsenderMottaker.getType(), is(AvsenderMottakerIdType.NULL));
        }

        @Test
        void shouldMapAvsenderMottakerIdTypeORGNRWhenAvsenderMottakerIdIsOfLength9() {
            JournalpostDto journalpostDto = buildJournalpostDtoAvsenderMottakerIdTypeNull();
            journalpostDto.setAvsenderMottakerId("123456789");

            AvsenderMottaker avsenderMottaker = mapper.map(journalpostDto);

            assertThat(avsenderMottaker.getType(), is(AvsenderMottakerIdType.ORGNR));
        }

        @ParameterizedTest
        @ValueSource(strings = {"***gammelt_fnr***", "***gammelt_fnr***", "***gammelt_fnr***", "***gammelt_fnr***",
                "***gammelt_fnr***", "***gammelt_fnr***", "***gammelt_fnr***", "***gammelt_fnr***"})
        void shouldMapAvsenderMottakerIdTypeFNRWhenAvsenderMottakerIdIs11DigitsLongAnd1DigitInRange0To7(String input) {
            JournalpostDto journalpostDto = buildJournalpostDtoAvsenderMottakerIdTypeNull();
            journalpostDto.setAvsenderMottakerId(input);

            AvsenderMottaker avsenderMottaker = mapper.map(journalpostDto);

            assertThat(avsenderMottaker.getType(), is(AvsenderMottakerIdType.FNR));
        }

        @ParameterizedTest
        @ValueSource(strings = {"***gammelt_fnr***", "***gammelt_fnr***"})
        @DisplayName("Test mapping av TSS-id")
        void shouldMapAvsenderMottakerIdTypeUKJENTWhenAvsenderMottakerIdIs11DigitsLongAndFirstDigitIs8Or9(String input) {
            JournalpostDto journalpostDto = buildJournalpostDtoAvsenderMottakerIdTypeNull();
            journalpostDto.setAvsenderMottakerId(input);

            AvsenderMottaker avsenderMottaker = mapper.map(journalpostDto);

            assertThat(avsenderMottaker.getType(), is(AvsenderMottakerIdType.UKJENT));
        }

        @ParameterizedTest
        @ValueSource(strings = {"EE:70000000"})
        @DisplayName("Test mapping av referanse til estiske trygdemyndigheter.")
        void shouldMapAvsenderMottakerIdTypeUKJENTWhenAvsenderMottakerIdIsLength11AndNonNumeric(String input) {
            JournalpostDto journalpostDto = buildJournalpostDtoAvsenderMottakerIdTypeNull();
            journalpostDto.setAvsenderMottakerId(input);

            AvsenderMottaker avsenderMottaker = mapper.map(journalpostDto);

            assertThat(avsenderMottaker.getType(), is(AvsenderMottakerIdType.UKJENT));
        }

        @ParameterizedTest
        @ValueSource(strings = {"12345", "***gammelt_fnr***23", ""})
        void shouldMapAvsenderMottakerIdTypeUKJENT(String input) {
            JournalpostDto journalpostDto = buildJournalpostDtoAvsenderMottakerIdTypeNull();
            journalpostDto.setAvsenderMottakerId(input);

            AvsenderMottaker avsenderMottaker = mapper.map(journalpostDto);

            assertThat(avsenderMottaker.getType(), is(AvsenderMottakerIdType.UKJENT));
        }

        private JournalpostDto buildJournalpostDtoAvsenderMottakerIdTypeNull() {
            JournalpostDto journalpostDto = JournalpostDtoTestObjects.buildJournalpostDtoInngaaendeType();
            journalpostDto.setAvsenderMottakerIdType(null);
            return journalpostDto;
        }
    }
}