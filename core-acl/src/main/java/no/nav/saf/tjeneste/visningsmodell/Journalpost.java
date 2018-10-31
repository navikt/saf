package no.nav.saf.tjeneste.visningsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.tjeneste.visningsmodell.kode.JournalStatus;
import no.nav.saf.tjeneste.visningsmodell.kode.JournalpostType;
import no.nav.saf.tjeneste.visningsmodell.kode.Mottakskanal;
import no.nav.saf.tjeneste.visningsmodell.kode.Temakode;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class Journalpost {
	private final String journalpostID;
	private final String beskrivelse;
	private final JournalpostType journalposttype;
	private final JournalStatus journalstatus;
	private final Temakode tema;
	private final String temanavn;
	private final Sak sak;
	private final Mottakskanal mottakskanal;
	private final String opprettet;
}
