package no.nav.saf.anticorruptionlayer.joark.safintern.journalpost;

import lombok.Builder;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;

import static no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode.PEN;

@Builder
public record ArkivSaksrelasjon(
		Long sakId,
		String fagsystem,
		Boolean feilregistrert,
		ArkivSak sak
) {
	public String getKey() {
		return sakId + FagsystemCode.toSafArkivsaksystem(fagsystem).toString();
	}
	public boolean isPensjonsak() {
		return PEN.name().equals(fagsystem);
	}
}
