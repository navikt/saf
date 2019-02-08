package no.nav.saf.domain.tilgangsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.kode.Skjerming;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class TilgangJournalpost {

	/**
	 * Attributter brukt for tilgangskontroll
	 **/
	private final Journalstatus journalstatus;
	private final Skjerming skjerming;
	@Builder.Default
	private final List<TilgangDokumentInfo> dokumenter = new ArrayList<>();

	/**
	 * Attributt brukt for å forenkle kodeflyt
	 **/
	private final String journalpostId;

}
