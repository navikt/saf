package no.nav.saf.anticorruptionlayer.joark.safintern.journalpost;

import java.util.List;

public record PaginatedArkivJournalpost(
		List<ArkivJournalpost> journalposter,
		int antallRader,
		int totaltAntallRader,
		int page,
		int totalPages,
		String nextPage
) {}
