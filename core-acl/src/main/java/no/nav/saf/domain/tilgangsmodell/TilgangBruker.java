package no.nav.saf.domain.tilgangsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.tilgangskontroll.abstraction.SecModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class TilgangBruker implements SecModel {
	private final String foedselsnr;
	private final String aktoerId;
	@Builder.Default
	private final List<TilgangIdent> historiskeIdenter = new ArrayList<>();
}
