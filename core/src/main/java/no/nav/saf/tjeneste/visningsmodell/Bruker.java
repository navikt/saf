package no.nav.saf.tjeneste.visningsmodell;

import lombok.Value;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class Bruker {
	private final String id;
	private final BrukerIdType type;
}
