package no.nav.saf.domain.tilgangsmodell;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class TilgangJournalpost {
	private final String journalpostId;
	private final String journalstatus;
	@Builder.Default
	private final List<TilgangDokumentInfo> dokumenter;
}
