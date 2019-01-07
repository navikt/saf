package no.nav.saf.domain.tilgangsmodell;

import lombok.Builder;
import lombok.Value;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class TilgangDokumentInfo {

	private final String dokumentInfoId;
	private final String journalpostId;
	private final String dokumentstatus;
	private final String brevkode;
	private final String variantFormat;
}
