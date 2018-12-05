package no.nav.saf.tjeneste.visningsmodell;

import lombok.Value;
import no.nav.saf.tjeneste.visningsmodell.kode.Brukertype;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class Bruker {
	private final Brukertype brukertype;
	private final String identifikator;
}
