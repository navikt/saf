package no.nav.saf.domain.tilgangsmodell;

import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class TilgangBruker {
	private final String foedselsnr;
	private final String aktoerId;
	@Builder.Default
	private final List<TilgangIdent> historiskeIdenter = new ArrayList<>();
}
