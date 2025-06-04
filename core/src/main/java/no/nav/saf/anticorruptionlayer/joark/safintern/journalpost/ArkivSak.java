package no.nav.saf.anticorruptionlayer.joark.safintern.journalpost;

import java.time.LocalDateTime;

public record ArkivSak(
		String tema,
		String aktoerId,
		String orgNr,
		String fagsakNr,
		String applikasjon,
		String sakStatus,
		LocalDateTime opprettetTid
) {
}
