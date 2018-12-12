package no.nav.saf.tjeneste.argumenter;

import lombok.Value;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class FagsakIdInput {
	private final String fagsaksnummer;
	private final String fagsaksystem;
}
