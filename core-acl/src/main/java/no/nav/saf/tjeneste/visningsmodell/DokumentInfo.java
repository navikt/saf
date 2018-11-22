package no.nav.saf.tjeneste.visningsmodell;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class DokumentInfo {
	private final String dokumentId;
	private final String tittel;
	private final String navSkjemaId;
	private final boolean saksbehandlerHarTilgang;
	@Builder.Default
	private final List<Dokumentvariant> dokumentvarianter;
}
