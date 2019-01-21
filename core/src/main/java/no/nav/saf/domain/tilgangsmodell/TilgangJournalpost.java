package no.nav.saf.domain.tilgangsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Journalposttype;
import no.nav.saf.domain.kode.Journalstatus;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class TilgangJournalpost {
	private final String arkivsaksnummer;
	private final Arkivsakssystem arkivsaksystem;
	private final String journalpostId;
	private final Journalstatus journalstatus;
	private final Journalposttype journalposttype;
	private final String tema;
	@Builder.Default
	private final List<TilgangDokumentInfo> dokumenter = new ArrayList<>();
}
