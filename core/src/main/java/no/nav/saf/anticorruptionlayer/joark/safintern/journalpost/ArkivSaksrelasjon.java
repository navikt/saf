package no.nav.saf.anticorruptionlayer.joark.safintern.journalpost;

import static no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode.PEN;

public record ArkivSaksrelasjon(Long sakId, String fagsystem, Boolean feilregistrert, ArkivSak sak) {
	public boolean isPensjonsak() {
		return PEN.name().equals(fagsystem);
	}
}
