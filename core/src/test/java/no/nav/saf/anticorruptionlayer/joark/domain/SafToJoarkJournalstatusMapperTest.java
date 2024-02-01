package no.nav.saf.anticorruptionlayer.joark.domain;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
import no.nav.saf.domain.kode.Journalstatus;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

class SafToJoarkJournalstatusMapperTest {
	private final SafToJoarkJournalstatusMapper mapper = new SafToJoarkJournalstatusMapper();

	@Test
	void shouldMapEmpty() {
		List<JournalStatusCode> journalStatusCodes = mapper.map(Collections.emptyList());
		assertThat(journalStatusCodes, hasSize(0));
	}

	@Test
	void shouldMapMottattToJoarkCodes() {
		List<JournalStatusCode> journalStatusCodes = mapper.map(Collections.singletonList(Journalstatus.MOTTATT));
		assertThat(journalStatusCodes, containsInAnyOrder(JournalStatusCode.M, JournalStatusCode.MO));
	}

	@Test
	void shouldMapJournalfoertToJoarkCodes() {
		List<JournalStatusCode> journalStatusCodes = mapper.map(Collections.singletonList(Journalstatus.JOURNALFOERT));
		assertThat(journalStatusCodes, containsInAnyOrder(JournalStatusCode.J));
	}

	@Test
	void shouldMapFerdigstiltToJoarkCodes() {
		List<JournalStatusCode> journalStatusCodes = mapper.map(Collections.singletonList(Journalstatus.FERDIGSTILT));
		assertThat(journalStatusCodes, containsInAnyOrder(JournalStatusCode.FL, JournalStatusCode.FS));
	}

	@Test
	void shouldMapEkspedertToJoarkCodes() {
		List<JournalStatusCode> journalStatusCodes = mapper.map(Collections.singletonList(Journalstatus.EKSPEDERT));
		assertThat(journalStatusCodes, containsInAnyOrder(JournalStatusCode.E));
	}

	@Test
	void shouldMapUnderArbeidToJoarkCodes() {
		List<JournalStatusCode> journalStatusCodes = mapper.map(Collections.singletonList(Journalstatus.UNDER_ARBEID));
		assertThat(journalStatusCodes, containsInAnyOrder(JournalStatusCode.D));
	}

	@Test
	void shouldMapFeilregistrertToJoarkCodes() {
		List<JournalStatusCode> journalStatusCodes = mapper.map(Collections.singletonList(Journalstatus.FEILREGISTRERT));
		assertThat(journalStatusCodes, hasSize(0));
	}

	@Test
	void shouldMapUtgaarToJoarkCodes() {
		List<JournalStatusCode> journalStatusCodes = mapper.map(Collections.singletonList(Journalstatus.UTGAAR));
		assertThat(journalStatusCodes, containsInAnyOrder(JournalStatusCode.U));
	}

	@Test
	void shouldMapAvbruttToJoarkCodes() {
		List<JournalStatusCode> journalStatusCodes = mapper.map(Collections.singletonList(Journalstatus.AVBRUTT));
		assertThat(journalStatusCodes, containsInAnyOrder(JournalStatusCode.A));
	}

	@Test
	void shouldMapUkjentbrukerToJoarkCodes() {
		List<JournalStatusCode> journalStatusCodes = mapper.map(Collections.singletonList(Journalstatus.UKJENT_BRUKER));
		assertThat(journalStatusCodes, containsInAnyOrder(JournalStatusCode.UB));
	}

	@Test
	void shouldMapReservertToJoarkCodes() {
		List<JournalStatusCode> journalStatusCodes = mapper.map(Collections.singletonList(Journalstatus.RESERVERT));
		assertThat(journalStatusCodes, containsInAnyOrder(JournalStatusCode.R));
	}

	@Test
	void shouldMapOpplastingDokumentToJoarkCodes() {
		List<JournalStatusCode> journalStatusCodes = mapper.map(Collections.singletonList(Journalstatus.OPPLASTING_DOKUMENT));
		assertThat(journalStatusCodes, containsInAnyOrder(JournalStatusCode.OD));
	}
}