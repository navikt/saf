package no.nav.saf.integration.penrest;

import no.nav.saf.domain.kode.Tema;

import java.net.URI;

public record Arkivtema (
		URI kodeverksRef,
		String value
) {
	static Arkivtema hardcoded() {
	return new Arkivtema(URI.create("local://hardcoded"), Tema.PEN.name());
}
}
