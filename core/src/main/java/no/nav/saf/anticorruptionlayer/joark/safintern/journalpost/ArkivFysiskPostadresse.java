package no.nav.saf.anticorruptionlayer.joark.safintern.journalpost;

import lombok.Builder;

@Builder
public record ArkivFysiskPostadresse(
		String adresselinje1,
		String adresselinje2,
		String adresselinje3,
		String landkode,
		String postnummer,
		String poststed

) {
}
