package no.nav.saf.tjeneste.visningsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.tjeneste.visningsmodell.kode.JournalStatus;
import no.nav.saf.tjeneste.visningsmodell.kode.JournalpostType;
import no.nav.saf.tjeneste.visningsmodell.kode.Kanal;
import no.nav.saf.tjeneste.visningsmodell.kode.Temakode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class Journalpost {
	private final String journalpostId;
	private final String tittel;
	private final JournalpostType journalposttype;
	private final JournalStatus journalstatus;
	private final Temakode tema;
	private final String temanavn;
	private final Sak sak;
	private final String avsenderMottakerNavn;
	private final String journalfortAvNavn;
	private final Kanal kanal;
	private final String kanalnavn;
	private final LocalDateTime opprettet;
	@Builder.Default
	private final List<DokumentInfo> dokumenter = new ArrayList<>();
}
