package no.nav.saf.domain.tilgangsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.tilgangskontroll.abstraction.SecModel;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class TilgangDokumentInfo implements SecModel {

	private final String dokumentInfoId;
	private final String journalpostId;
	private final String dokumentstatus;
	private final String brevkode;
	private final String variantFormat;
}
