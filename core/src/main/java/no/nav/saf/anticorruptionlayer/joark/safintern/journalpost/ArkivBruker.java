package no.nav.saf.anticorruptionlayer.joark.safintern.journalpost;

import static no.nav.saf.anticorruptionlayer.joark.domain.kode.BrukerTypeCode.PERSON;

public record ArkivBruker(String id, String type) {
	public boolean isPerson() {
		return PERSON.equals(type);
	}
}
