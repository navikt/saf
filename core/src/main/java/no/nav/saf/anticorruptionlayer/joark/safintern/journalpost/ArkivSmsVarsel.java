package no.nav.saf.anticorruptionlayer.joark.safintern.journalpost;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ArkivSmsVarsel(
		String tekst,
		String mobilnummer,
		LocalDateTime varslingstidspunkt
) {
}
