package no.nav.saf.tjeneste.visningsmodell;

import lombok.Builder;
import lombok.Value;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class Bruker {
	private final String aktoerId;
}
