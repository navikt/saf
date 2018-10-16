package no.nav.saf.context.saf.domain;

import lombok.Builder;
import lombok.Data;
import no.nav.saf.context.saf.domain.kode.JournalpostStatus;
import no.nav.saf.context.saf.domain.kode.JournalpostType;
import no.nav.saf.context.saf.domain.kode.Mottakskanal;
import no.nav.saf.context.saf.domain.kode.Utsendingskanal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class Journalpost {
	private final String journalpostID;
	private final JournalpostType journalposttype;
	private final JournalpostStatus journalstatus;
	private final Mottakskanal mottakskanal;
	private final Utsendingskanal utsendingskanal;
	private final String journalposttittel;
	private final String avsenderID;
	private final String avsenderNavn;
	private final LocalDateTime opprettet;

	@Builder.Default
	private final List<DokumentInfo> dokumenter = new ArrayList<>();
}
