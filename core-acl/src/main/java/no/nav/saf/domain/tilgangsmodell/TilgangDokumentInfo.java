package no.nav.saf.domain.tilgangsmodell;

import lombok.Value;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class TilgangDokumentInfo {
	private final String dokumentInfoId;
	private final String dokumentstatus;
	private final String variantFormat;
}
