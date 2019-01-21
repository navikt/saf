package no.nav.saf.domain.visningsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.domain.kode.Variantformat;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class Dokumentvariant {

	private final Variantformat variantformat;
	private final boolean saksbehandlerHarTilgang;
}
