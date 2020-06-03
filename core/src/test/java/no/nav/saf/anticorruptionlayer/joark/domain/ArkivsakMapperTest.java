package no.nav.saf.anticorruptionlayer.joark.domain;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.BrukerDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.SaksrelasjonDto;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.DomainConstants;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author Erik Bråten, Visma Consulting
 */
class ArkivsakMapperTest {

	private static final long JOURNALPOST_ID = 123456789L;
	private static final FagomradeCode FAGOMRADE = FagomradeCode.STO;

	private static final String SAK_ID = "11223344";
	private static final FagsystemCode FAGSYSTEM = FagsystemCode.FS22;

	private static final String AKTOER_ID = "***gammelt_fnr***";
	private static final String ORGNR = "123123123";
	private static final String BRUKER_ID_TYPE = DomainConstants.PERSON;

	private final ArkivsakMapper mapper = new ArkivsakMapper();

	@Test
	void shouldMapNull() {
		Arkivsak arkivsak = mapper.map(null);
		assertNull(arkivsak);
	}

	@Test
	void shouldMapOk() {
		JournalpostDto journalpostDto = buildJournalpost();
		Arkivsak arkivsak = mapper.map(journalpostDto);

		assertEquals(SAK_ID, arkivsak.getArkivsaksnummer());
		assertEquals(FAGSYSTEM.name(), arkivsak.getFagsaksystem());
		assertEquals(FagsystemCode.toSafArkivsaksystem(FAGSYSTEM), arkivsak.getArkivsaksystem());
		assertEquals(AKTOER_ID, arkivsak.getAktoerId());
		assertNull(arkivsak.getOrgnummer());
		assertEquals(FagomradeCode.toSafTema(FAGOMRADE), arkivsak.getTema());
		assertNull(arkivsak.getFagsakId());
	}

	@Test
	void shouldMapNoSaksrelasjon() {
		Arkivsak arkivsak = mapper.map(null);
		assertNull(arkivsak);
	}

	@Test
	void shouldMapNoBruker() {
		JournalpostDto journalpostDto = buildJournalpost();
		journalpostDto.setBruker(null);
		Arkivsak arkivsak = mapper.map(journalpostDto);

		assertNull(arkivsak.getAktoerId());
		assertNull(arkivsak.getOrgnummer());
		assertEquals(SAK_ID, arkivsak.getArkivsaksnummer());
	}

	@Test
	void shouldMapWithOrgnr() {
		JournalpostDto journalpostDto = buildJournalpost();
		journalpostDto.getBruker().setBrukerId(ORGNR);
		journalpostDto.getBruker().setBrukerIdType(DomainConstants.ORGANISASJON);
		Arkivsak arkivsak = mapper.map(journalpostDto);

		assertNull(arkivsak.getAktoerId());
		assertEquals(ORGNR, arkivsak.getOrgnummer());
		assertEquals(SAK_ID, arkivsak.getArkivsaksnummer());
	}

	private JournalpostDto buildJournalpost() {
		return JournalpostDto.builder()
				.journalpostId(JOURNALPOST_ID)
				.fagomrade(FAGOMRADE)
				.saksrelasjon(buildSaksrelasjon())
				.bruker(buildBruker())
				.build();
	}

	private SaksrelasjonDto buildSaksrelasjon() {
		return SaksrelasjonDto.builder()
				.sakId(SAK_ID)
				.fagsystem(FAGSYSTEM)
				.build();
	}

	private BrukerDto buildBruker() {
		return BrukerDto.builder()
				.brukerId(AKTOER_ID)
				.brukerIdType(BRUKER_ID_TYPE)
				.build();
	}
}
