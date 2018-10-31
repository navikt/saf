package no.nav.saf.tjeneste.visningsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.tjeneste.visningsmodell.kode.VariantFormatkode;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class DokumentInfo {
    private final String dokumentID;
    private final String tittel;
    private final VariantFormatkode variantFormat;
    private final boolean saksbehandlerHarTilgang;
    private final boolean innbyggerHarDigitaltInnsyn;
}
