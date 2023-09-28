package no.nav.saf.anticorruptionlayer.joark.safintern.journalpost;

import static no.nav.saf.domain.DomainConstants.PERSON;

public record ArkivBruker(String id, String type) {
	public boolean isPerson() {
		return PERSON.equals(type);
	}
}
