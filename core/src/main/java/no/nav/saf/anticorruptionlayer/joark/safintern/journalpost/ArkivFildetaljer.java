package no.nav.saf.anticorruptionlayer.joark.safintern.journalpost;

import lombok.Builder;

@Builder
public record ArkivFildetaljer(
		// brukt til tilgangskontroll i hentdokument
		String skjerming,
		String format,
		//
		String navn,
		String stoerrelse,
		String type,
		String uuid
) {
}
