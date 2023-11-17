package no.nav.saf.anticorruptionlayer.joark.safintern.journalpost;

import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record ArkivRelevanteDatoer(
		OffsetDateTime opprettet,
		OffsetDateTime journalfoert,
		OffsetDateTime ekspedert,
		OffsetDateTime forsendelseMottatt,
		OffsetDateTime hoveddokument,
		OffsetDateTime lest,
		OffsetDateTime retur,
		OffsetDateTime sendtPrint
) {
}
