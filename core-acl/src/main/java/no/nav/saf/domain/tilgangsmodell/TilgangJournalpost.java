package no.nav.saf.domain.tilgangsmodell;

import lombok.Value;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class TilgangJournalpost {
	private final String journalpostId;
	private final String journalstatus;
	private final TilgangBruker bruker;
	private final List<TilgangDokumentInfo> dokumenter;
}
