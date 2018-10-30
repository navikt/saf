package no.nav.saf.tilgangskontroll;

import lombok.Builder;
import lombok.Data;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class SafRequestContext {
	private final String oidcToken;
	private final NavBrukertype navBrukertype;
	private String aktoerId;
}
