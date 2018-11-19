package no.nav.saf.tjeneste.visningsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.tjeneste.visningsmodell.kode.VariantFormat;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class DokumentInfo {
    private final String dokumentId;
    private final String tittel;
    private final VariantFormat variantFormat;
    private final boolean saksbehandlerHarTilgang;
    private final boolean innbyggerHarDigitaltInnsyn;
}
