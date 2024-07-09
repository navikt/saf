package no.nav.saf.anticorruptionlayer.joark.safintern.journalpost;

import java.util.List;

public record JournalpostJournalstatusRequest(
		String journalstatus,
		String fraDato,
		List<String> journalposttyper,
		Integer antallRader,
		String etterPeker
) {}
