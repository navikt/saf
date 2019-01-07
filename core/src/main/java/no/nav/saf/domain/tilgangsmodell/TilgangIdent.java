package no.nav.saf.domain.tilgangsmodell;

import lombok.Builder;
import lombok.Value;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class TilgangIdent {
	private final String identifikator;
}
