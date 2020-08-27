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
	private static final String ARKIVSAKID = "11223344";
	private static final FagsystemCode ARKIVSAKSYSTEM = FagsystemCode.FS22;
	private static final String FAGSAKID = "A1234";
	private static final String FAGSAKSYSTEM = "K9";
	private static final String AKTOER_ID = "12312312312";
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

		assertEquals(ARKIVSAKID, arkivsak.getArkivsaksnummer());
		assertEquals(FagsystemCode.toSafArkivsaksystem(ARKIVSAKSYSTEM), arkivsak.getArkivsaksystem());
		assertEquals(FAGSAKID, arkivsak.getFagsakId());
		assertEquals(FAGSAKSYSTEM, arkivsak.getFagsaksystem());
		assertEquals(AKTOER_ID, arkivsak.getAktoerId());
		assertNull(arkivsak.getOrgnummer());
		assertEquals(FagomradeCode.toSafTema(FAGOMRADE), arkivsak.getTema());
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
		assertEquals(ARKIVSAKID, arkivsak.getArkivsaksnummer());
	}

	@Test
	void shouldMapWithOrgnr() {
		JournalpostDto journalpostDto = buildJournalpost();
		journalpostDto.getBruker().setBrukerId(ORGNR);
		journalpostDto.getBruker().setBrukerIdType(DomainConstants.ORGANISASJON);
		Arkivsak arkivsak = mapper.map(journalpostDto);

		assertNull(arkivsak.getAktoerId());
		assertEquals(ORGNR, arkivsak.getOrgnummer());
		assertEquals(ARKIVSAKID, arkivsak.getArkivsaksnummer());
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
				.sakId(ARKIVSAKID)
				.fagsystem(ARKIVSAKSYSTEM)
				.fagsakNr(FAGSAKID)
				.applikasjon(FAGSAKSYSTEM)
				.build();
	}

	private BrukerDto buildBruker() {
		return BrukerDto.builder()
				.brukerId(AKTOER_ID)
				.brukerIdType(BRUKER_ID_TYPE)
				.build();
	}
}
