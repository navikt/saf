package no.nav.saf.anticorruptionlayer.joark.safintern.journalpost;

import lombok.Builder;
import no.nav.safselvbetjening.tilgang.TilgangSkjermingType;
import no.nav.safselvbetjening.tilgang.TilgangVariant;
import no.nav.safselvbetjening.tilgang.TilgangVariantFormat;

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

	public TilgangVariant getTilgangVariant() {
		return TilgangVariant.builder()
				.variantformat(TilgangVariantFormat.from(format))
				.skjerming(TilgangSkjermingType.from(skjerming))
				.build();
	}
}
