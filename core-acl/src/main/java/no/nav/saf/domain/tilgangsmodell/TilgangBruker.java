package no.nav.saf.domain.tilgangsmodell;

import lombok.Value;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class TilgangBruker {
	private final String aktoerId;
	private final List<TilgangIdent> historiskeIdenter;
}
