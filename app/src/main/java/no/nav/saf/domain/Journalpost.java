package no.nav.saf.domain;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class Journalpost {
	private final String journalpostId;
	private final String beskrivelse;
	private final String avsender;
	private final Temakode tema;
	private final JournalpostType type;
	private final JournalpostStatus status;
	private final boolean feilregistrert;
	@Builder.Default
	private final List<DokumentInfo> dokumentInfo = new ArrayList<>();
}
