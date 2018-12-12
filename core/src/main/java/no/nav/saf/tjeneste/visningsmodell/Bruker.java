package no.nav.saf.tjeneste.visningsmodell;

import lombok.Value;
import no.nav.saf.tjeneste.argumenter.BrukerIdType;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class Bruker {
	private final String id;
	private final BrukerIdType type;
}
