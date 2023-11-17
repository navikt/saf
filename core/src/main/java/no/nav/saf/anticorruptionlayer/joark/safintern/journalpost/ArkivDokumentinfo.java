package no.nav.saf.anticorruptionlayer.joark.safintern.journalpost;

import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Builder
public record ArkivDokumentinfo(
		// tilgangskontroll
		Long dokumentInfoId,
		String skjerming,
		//
		String brevkode,
		String dokumenttypeId,
		OffsetDateTime ferdigDato,
		Long id,
		Boolean kassert,
		String kategori,
		Set<ArkivLogiskVedlegg> logiskVedlegg,
		Long originalJournalpostId,
		Boolean sensitivt,
		String status,
		String tilknyttetSom,
		String tittel,
		List<ArkivFildetaljer> fildetaljer
) {
}
