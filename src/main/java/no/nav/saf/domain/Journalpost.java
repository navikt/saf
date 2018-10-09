package no.nav.saf.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class Journalpost {
	private final String journalpostId;
	private final String innhold;
	private final String kanalReferanseId;
	private LocalDateTime datoJournalfoert;
	private final Mottakskanal mottakskanal;
	private final Utsendingskanal utsendingskanal;
	private final String journalfoerendeEnhet;
	private final String avsender;
	private final String avsenderNavn;
	private final AvsenderType avsenderType;
	private final Temakode tema;
	private final JournalpostType type;
	private final JournalTilstand journalTilstand;
	private final JournalpostStatus status;
	private final boolean feilregistrert;
	@Builder.Default
	private final List<DokumentInfo> dokumentInfo = new ArrayList<>();
}
