package no.nav.saf.domain.tilgangsmodell;

import lombok.Value;
import no.nav.saf.tilgangskontroll.abstraction.SecModel;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class TilgangRelevantTredjepart implements SecModel {
	private final TilgangIdent ident;
}
