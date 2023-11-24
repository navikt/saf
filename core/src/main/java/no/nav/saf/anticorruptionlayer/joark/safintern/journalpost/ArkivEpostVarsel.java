package no.nav.saf.anticorruptionlayer.joark.safintern.journalpost;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ArkivEpostVarsel(
		String tittel,
		String tekst,
		String epostadresse,
		LocalDateTime varslingstidspunkt
) {
}
